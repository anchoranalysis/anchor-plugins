/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider.stack;

import java.nio.ByteBuffer;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.factory.VoxelBoxFactory;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.plugin.image.bean.object.provider.ObjectCollectionProviderWithDimensions;

/**
 * Creates a 3D version of an object by replicating each input-object across the z-dimension to meet
 * (3D) dimensions.
 *
 * <p>An object-collection is produced with an identical number of objects, but with each expanded
 * in the z-dimension.
 *
 * <p>If the input-object is a 2D-slice it is replicated directly, if it is already a 3D-object its
 * flattened version (a maximum intensity projection) is used.
 *
 * @author Owen Feehan
 */
public class ExtendInZ extends ObjectCollectionProviderWithDimensions {

    @Override
    public ObjectCollection createFromObjects(ObjectCollection objects) throws CreateException {

        ImageDimensions dimensions = createDimensions();

        return objects.stream().map(objectMask -> expandZ(objectMask.flattenZ(), dimensions));
    }

    private static ObjectMask expandZ(ObjectMask object, ImageDimensions dim) {

        BoundingBox bbox = object.getBoundingBox().duplicateChangeExtentZ(dim.getZ());

        VoxelBox<ByteBuffer> voxelBox =
                createVoxelBoxOfDuplicatedPlanes(
                        object.getVoxelBox().getPixelsForPlane(0), bbox.extent());

        return new ObjectMask(bbox, voxelBox, object.getBinaryValues());
    }

    private static VoxelBox<ByteBuffer> createVoxelBoxOfDuplicatedPlanes(
            VoxelBuffer<ByteBuffer> planeIn, Extent extent) {
        VoxelBox<ByteBuffer> voxelBox = VoxelBoxFactory.getByte().create(extent);
        for (int z = 0; z < extent.getZ(); z++) {
            voxelBox.setPixelsForPlane(z, planeIn);
        }
        return voxelBox;
    }
}
