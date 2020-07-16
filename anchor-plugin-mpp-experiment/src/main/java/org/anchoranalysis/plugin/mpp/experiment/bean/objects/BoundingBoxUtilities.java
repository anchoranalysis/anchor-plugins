/* (C)2020 */
package org.anchoranalysis.plugin.mpp.experiment.bean.objects;

import java.nio.ByteBuffer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.factory.VoxelBoxFactory;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class BoundingBoxUtilities {

    /**
     * Creates an object-mask for a possibly differently sized bounding box that it currently exists
     * within.
     *
     * @param object the object-mask
     * @param boundingBox a possibly differently sized bounding box
     * @return the object-mask unchanged if the bounding-box is identical to current, otherwise a
     *     new object-mask to match the desired bounding-box
     */
    public static ObjectMask createObjectForBoundingBox(
            ObjectMask object, BoundingBox boundingBox) {

        if (object.getBoundingBox().equals(boundingBox)) {
            // Nothing to do, bounding-boxes are equal, early exit
            return object;
        }

        VoxelBox<ByteBuffer> vbLarge = VoxelBoxFactory.getByte().create(boundingBox.extent());

        BoundingBox bbLocal = object.getBoundingBox().relPosToBox(boundingBox);

        BinaryValuesByte bvb = BinaryValuesByte.getDefault();
        vbLarge.setPixelsCheckMask(
                new ObjectMask(bbLocal, object.binaryVoxelBox()), bvb.getOnByte());

        return new ObjectMask(boundingBox, vbLarge, bvb);
    }
}
