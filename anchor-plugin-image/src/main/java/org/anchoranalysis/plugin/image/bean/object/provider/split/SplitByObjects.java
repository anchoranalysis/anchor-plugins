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
import org.anchoranalysis.image.voxel.binary.values.BinaryValuesInt;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedIntBuffer;
import org.anchoranalysis.image.voxel.factory.VoxelsFactory;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectCollectionFactory;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.object.provider.WithDimensionsBase;
import org.anchoranalysis.spatial.box.Extent;

/**
 * Splits objects in a collection based on intersections with another set of objects.
 *
 * <p>This class extends {@link WithDimensionsBase} to provide functionality for splitting objects
 * in one collection based on their intersections with objects in another collection.
 */
public class SplitByObjects extends WithDimensionsBase {

    /** Factory for creating objects from connected components. */
    private static final ObjectsFromConnectedComponentsFactory CONNECTED_COMPONENTS_CREATOR =
            new ObjectsFromConnectedComponentsFactory();

    // START BEAN PROPERTIES
    /** Provider for the collection of objects used to split the input objects. */
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
                                        findObjectsWithIntersectingBoundingBox(
                                                objectsSplitByCollection, object),
                                        dimensions.extent()));
    }

    /**
     * Splits a single object based on its intersections with a collection of other objects.
     *
     * @param objectToSplit The {@link ObjectMask} to be split.
     * @param objectsSplitBy The {@link ObjectCollection} used to split the object.
     * @param extent The {@link Extent} of the image space.
     * @return A new {@link ObjectCollection} containing the split objects.
     */
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
     * Performs a flood fill for each identifier in the voxel buffer.
     *
     * @param count The number of unique identifiers.
     * @param voxelsWithIdentifiers The {@link BoundedVoxels} containing identifiers.
     * @return An {@link ObjectCollection} of flood-filled objects.
     */
    private ObjectCollection floodFillEachIdentifier(
            int count, BoundedVoxels<UnsignedIntBuffer> voxelsWithIdentifiers) {
        return ObjectCollectionFactory.flatMapFromRange(
                1, count, index -> createObjectForIndex(index, voxelsWithIdentifiers));
    }

    /**
     * Creates objects from all connected components in a buffer with particular voxel values.
     *
     * @param voxelEqualTo The voxel value to consider as part of the object.
     * @param voxels The {@link BoundedVoxels} containing the voxel data.
     * @return An {@link ObjectCollection} of created objects.
     */
    private static ObjectCollection createObjectForIndex(
            int voxelEqualTo, BoundedVoxels<UnsignedIntBuffer> voxels) {
        BinaryVoxels<UnsignedIntBuffer> binaryVoxels =
                BinaryVoxelsFactory.reuseInt(voxels.voxels(), new BinaryValuesInt(0, voxelEqualTo));

        // for every object we add the objToSplit Bounding Box corner, to restore it to global
        // coordinates
        return CONNECTED_COMPONENTS_CREATOR
                .createUnsignedInt(binaryVoxels)
                .shiftBy(voxels.cornerMin());
    }

    /**
     * Finds objects in a collection that have bounding boxes intersecting with a given object.
     *
     * @param objects The {@link ObjectCollection} to search in.
     * @param toIntersectWith The {@link ObjectMask} to intersect with.
     * @return An {@link ObjectCollection} of objects with intersecting bounding boxes.
     */
    private static ObjectCollection findObjectsWithIntersectingBoundingBox(
            ObjectCollection objects, ObjectMask toIntersectWith) {
        return objects.stream()
                .filter(
                        object ->
                                object.boundingBox()
                                        .intersection()
                                        .existsWith(toIntersectWith.boundingBox()));
    }
}
