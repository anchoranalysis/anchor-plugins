/* (C)2020 */
package ch.ethz.biol.cell.sgmn.binary;

import java.nio.ByteBuffer;
import java.util.Optional;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.bean.nonbean.parameters.BinarySegmentationParameters;
import org.anchoranalysis.image.bean.segment.binary.BinarySegmentation;
import org.anchoranalysis.image.bean.segment.binary.BinarySegmentationOne;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;

public class SgmnInv extends BinarySegmentationOne {

    @Override
    public BinaryVoxelBox<ByteBuffer> sgmnFromSgmn(
            VoxelBoxWrapper voxelBox,
            BinarySegmentationParameters params,
            Optional<ObjectMask> mask,
            BinarySegmentation sgmn)
            throws SegmentationFailedException {

        BinaryVoxelBox<ByteBuffer> vb = sgmn.sgmn(voxelBox, params, mask);

        try {
            invertVoxelBox(vb);
        } catch (OperationFailedException e) {
            throw new SegmentationFailedException(e);
        }

        return vb;
    }

    private void invertVoxelBox(BinaryVoxelBox<ByteBuffer> voxelBox)
            throws OperationFailedException {

        BinaryValuesByte bv = voxelBox.getBinaryValues().createByte();

        int volumeXY = voxelBox.extent().getVolumeXY();

        // We invert each item in the VoxelBox
        for (int z = 0; z < voxelBox.extent().getZ(); z++) {

            ByteBuffer bb = voxelBox.getPixelsForPlane(z).buffer();
            for (int index = 0; index < volumeXY; index++) {

                byte val = bb.get(index);

                if (val == bv.getOnByte()) {
                    bb.put(index, bv.getOffByte());
                } else if (val == bv.getOffByte()) {
                    bb.put(index, bv.getOnByte());
                } else {
                    assert false;
                }
            }
        }
    }
}
