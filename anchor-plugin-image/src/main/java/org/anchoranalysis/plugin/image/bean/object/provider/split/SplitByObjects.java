/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider.split;

import java.nio.IntBuffer;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.ReadableTuple3i;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBoxInt;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.factory.CreateFromConnectedComponentsFactory;
import org.anchoranalysis.image.voxel.box.BoundedVoxelBox;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.factory.VoxelBoxFactory;
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
                    .flatMapWithException(
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

        // Should be set to 0 by default
        BoundedVoxelBox<IntBuffer> boundedVbId =
                new BoundedVoxelBox<>(
                        VoxelBoxFactory.getInt().create(objectToSplit.getBoundingBox().extent()));

        // Populate boundedVbId with id values
        int cnt = 1;
        for (ObjectMask objectLocal : objectsSplitBy) {

            Optional<ObjectMask> intersect = objectToSplit.intersect(objectLocal, dim);

            // If there's no intersection, there's nothing to do
            if (!intersect.isPresent()) {
                continue;
            }

            ObjectMask intersectShifted =
                    intersect
                            .get()
                            .mapBoundingBox(
                                    bbox ->
                                            bbox.shiftBackBy(
                                                    objectToSplit.getBoundingBox().cornerMin()));

            // We make the intersection relative to objToSplit
            boundedVbId.getVoxelBox().setPixelsCheckMask(intersectShifted, cnt++);
        }

        try {
            // Now we do a flood fill for each number, pretending it's a binary image of 0 and i
            // The code will not change pixels that don't match ON
            return ObjectCollectionFactory.flatMapFromRange(
                    1,
                    cnt,
                    CreateException.class,
                    i ->
                            createObjectForIndex(
                                    i,
                                    boundedVbId.getVoxelBox(),
                                    objectToSplit.getBoundingBox().cornerMin()));

        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }

    /** Creates objects from all connected-components in a buffer with particular voxel values */
    private static ObjectCollection createObjectForIndex(
            int voxelEqualTo, VoxelBox<IntBuffer> voxels, ReadableTuple3i shiftBy)
            throws CreateException {
        BinaryVoxelBox<IntBuffer> binaryVoxels =
                new BinaryVoxelBoxInt(voxels, new BinaryValues(0, voxelEqualTo));

        // for every object we add the objToSplit Bounding Box crnr, to restore it to global
        // coordinates
        return CONNECTED_COMPONENTS_CREATOR.create(binaryVoxels).shiftBy(shiftBy);
    }
}
