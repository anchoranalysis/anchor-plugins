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

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.voxel.BoundedVoxels;
import org.anchoranalysis.image.voxel.binary.BinaryVoxels;
import org.anchoranalysis.image.voxel.binary.BinaryVoxelsFactory;
import org.anchoranalysis.image.voxel.binary.connected.ObjectsFromConnectedComponentsFactory;
import org.anchoranalysis.image.voxel.binary.values.BinaryValues;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedIntBuffer;
import org.anchoranalysis.image.voxel.factory.VoxelsFactory;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectCollectionFactory;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.object.provider.WithDimensionsBase;
import org.anchoranalysis.spatial.Extent;

public class SplitByObjects extends WithDimensionsBase {

    private static final ObjectsFromConnectedComponentsFactory CONNECTED_COMPONENTS_CREATOR =
            new ObjectsFromConnectedComponentsFactory();

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ObjectCollectionProvider objectsSplitBy;
    // END BEAN PROPERTIES

    @Override
    public ObjectCollection createFromObjects(ObjectCollection objectCollection)
            throws ProvisionFailedException {

        ObjectCollection objectsSplitByCollection = objectsSplitBy.get();

        Dimensions dimensions = createDimensions();

        return objectCollection.stream()
                .flatMap(
                        OperationFailedException.class,
                        object ->
                                splitObject(
                                        object,
                                        objectsSplitByCollection.findObjectsWithIntersectingBBox(
                                                object),
                                        dimensions.extent()));
    }

    private ObjectCollection splitObject(
            ObjectMask objectToSplit, ObjectCollection objectsSplitBy, Extent extent) {

        // We create a voxel buffer of the same size as objectToSplit bounding box, and we write
        //  a number for each object in objectsSplitBy
        // Then we find connected components

        // An Integer buffer with 0 by default and the same bounds as the object to be split
        BoundedVoxels<UnsignedIntBuffer> voxelsWithIdentifiers =
                VoxelsFactory.getUnsignedInt().createBounded(objectToSplit.boundingBox());

        // Populate boundedVbId with id values
        int count = 1;
        for (ObjectMask objectLocal : objectsSplitBy) {

            Optional<ObjectMask> intersect = objectToSplit.intersect(objectLocal, extent);

            // If there's no intersection, there's nothing to do
            if (intersect.isPresent()) {
                voxelsWithIdentifiers.assignValue(count++).toObject(intersect.get());
            }
        }

        return floodFillEachIdentifier(count, voxelsWithIdentifiers);
    }

    /**
     * Perform a flood fill for each number, pretending it's a binary image of 0 and i
     *
     * <p>The code will not change pixels that don't match ON
     */
    private ObjectCollection floodFillEachIdentifier(
            int count, BoundedVoxels<UnsignedIntBuffer> voxelsWithIdentifiers) {
        return ObjectCollectionFactory.flatMapFromRange(
                1, count, index -> createObjectForIndex(index, voxelsWithIdentifiers));
    }

    /** Creates objects from all connected-components in a buffer with particular voxel values */
    private static ObjectCollection createObjectForIndex(
            int voxelEqualTo, BoundedVoxels<UnsignedIntBuffer> voxels) {
        BinaryVoxels<UnsignedIntBuffer> binaryVoxels =
                BinaryVoxelsFactory.reuseInt(voxels.voxels(), new BinaryValues(0, voxelEqualTo));

        // for every object we add the objToSplit Bounding Box corner, to restore it to global
        // coordinates
        return CONNECTED_COMPONENTS_CREATOR
                .createUnsignedInt(binaryVoxels)
                .shiftBy(voxels.cornerMin());
    }
}
