/* (C)2020 */
package ch.ethz.biol.cell.sgmn.binary;

import java.nio.ByteBuffer;
import org.anchoranalysis.core.geometry.ReadableTuple3i;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;

final class SliceThresholderMask extends SliceThresholder {

    private final boolean clearOutsideMask;
    private final ObjectMask object;
    private final ReadableTuple3i cornerMin;
    private final ReadableTuple3i cornerMax;

    public SliceThresholderMask(boolean clearOutsideMask, ObjectMask object, BinaryValuesByte bvb) {
        super(bvb);
        this.clearOutsideMask = clearOutsideMask;
        this.object = object;
        this.cornerMin = object.getBoundingBox().cornerMin();
        this.cornerMax = object.getBoundingBox().calcCornerMax();
    }

    @Override
    public void sgmnAll(
            VoxelBox<?> voxelBoxIn, VoxelBox<?> vbThrshld, VoxelBox<ByteBuffer> voxelBoxOut) {
        for (int z = cornerMin.getZ(); z <= cornerMax.getZ(); z++) {

            int relZ = z - cornerMin.getZ();

            sgmnSlice(
                    voxelBoxIn.extent(),
                    voxelBoxIn.getPixelsForPlane(relZ),
                    vbThrshld.getPixelsForPlane(relZ),
                    voxelBoxOut.getPixelsForPlane(relZ),
                    object.getVoxelBox().getPixelsForPlane(z),
                    object.getBinaryValuesByte());
        }
    }

    private void sgmnSlice(
            Extent extent,
            VoxelBuffer<?> vbIn,
            VoxelBuffer<?> vbThrshld,
            VoxelBuffer<ByteBuffer> vbOut,
            VoxelBuffer<ByteBuffer> vbMask,
            BinaryValuesByte bvbMask) {
        int offsetMask = 0;
        ByteBuffer out = vbOut.buffer();

        for (int y = cornerMin.getY(); y <= cornerMax.getY(); y++) {
            for (int x = cornerMin.getX(); x <= cornerMax.getX(); x++) {

                int offset = extent.offset(x, y);

                if (vbMask.buffer().get(offsetMask++) == bvbMask.getOffByte()) {

                    if (clearOutsideMask) {
                        writeOffByte(offset, out);
                    }

                    continue;
                }

                writeThresholdedByte(offset, out, vbIn, vbThrshld);
            }
        }
    }
}
