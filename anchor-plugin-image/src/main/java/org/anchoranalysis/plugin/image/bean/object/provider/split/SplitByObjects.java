/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

package org.anchoranalysis.plugin.image.bean.object.provider.split;

import java.nio.IntBuffer;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.binary.voxel.BinaryVoxels;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelsFactory;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.factory.CreateFromConnectedComponentsFactory;
import org.anchoranalysis.image.voxel.BoundedVoxels;
import org.anchoranalysis.image.voxel.factory.VoxelsFactory;
import org.anchoranalysis.plugin.image.bean.object.provider.ObjectCollectionProviderWithDimensions;

public class SplitByObjects extends ObjectCollectionProviderWithDimensions {

    private static final CreateFromConnectedComponentsFactory CONNECTED_COMPONENTS_CREATOR =
            new CreateFromConnectedComponentsFactory();

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ObjectCollectionProvider objectsSplitBy;
    // END BEAN PROPERTIES

    @Override
    public ObjectCollection createFromObjects(ObjectCollection objectCollection)
            throws CreateException {

        ObjectCollection objectsSplitByCollection = objectsSplitBy.create();

        ImageDimensions dimensions = createDimensions();

        try {
            return objectCollection.stream()
                    .flatMap(
                            OperationFailedException.class,
                            object ->
                                    splitObject(
                                            object,
                                            objectsSplitByCollection
                                                    .findObjectsWithIntersectingBBox(object),
                                            dimensions));
        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }

    private ObjectCollection splitObject(
            ObjectMask objectToSplit, ObjectCollection objectsSplitBy, ImageDimensions dim)
            throws OperationFailedException {

        // We create a voxel buffer of the same size as objToSplit bounding box, and we write
        //  a number for each object in objectsSplitBy
        // Then we find connected components

        // An Integer buffer with 0 by default and the same bounds as the object to be split
        BoundedVoxels<IntBuffer> voxelsId =
                new BoundedVoxels<>(
                        objectToSplit.boundingBox(),
                        VoxelsFactory.getInt()
                                .createInitialized(objectToSplit.boundingBox().extent()));

        // Populate boundedVbId with id values
        int cnt = 1;
        for (ObjectMask objectLocal : objectsSplitBy) {

            Optional<ObjectMask> intersect = objectToSplit.intersect(objectLocal, dim);

            // If there's no intersection, there's nothing to do
            if (intersect.isPresent()) {
                voxelsId.assignValue(cnt++).toObject(intersect.get());
            }
        }

        try {
            // Now we do a flood fill for each number, pretending it's a binary image of 0 and i
            // The code will not change pixels that don't match ON
            return ObjectCollectionFactory.flatMapFromRange(
                    1, cnt, CreateException.class, i -> createObjectForIndex(i, voxelsId));

        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }

    /** Creates objects from all connected-components in a buffer with particular voxel values */
    private static ObjectCollection createObjectForIndex(
            int voxelEqualTo, BoundedVoxels<IntBuffer> voxels) throws CreateException {
        BinaryVoxels<IntBuffer> binaryVoxels =
                BinaryVoxelsFactory.reuseInt(voxels.voxels(), new BinaryValues(0, voxelEqualTo));

        // for every object we add the objToSplit Bounding Box corner, to restore it to global
        // coordinates
        return CONNECTED_COMPONENTS_CREATOR.create(binaryVoxels).shiftBy(voxels.cornerMin());
    }
}
