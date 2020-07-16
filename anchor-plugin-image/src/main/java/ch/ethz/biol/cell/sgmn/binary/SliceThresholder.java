/* (C)2020 */
package ch.ethz.biol.cell.sgmn.binary;

import java.nio.ByteBuffer;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;

public abstract class SliceThresholder {

    private final BinaryValuesByte bvb;

    public SliceThresholder(BinaryValuesByte bvb) {
        super();
        this.bvb = bvb;
    }

    public abstract void sgmnAll(
            VoxelBox<?> voxelBoxIn, VoxelBox<?> vbThrshld, VoxelBox<ByteBuffer> voxelBoxOut);

    protected final void writeOffByte(int offset, ByteBuffer bbOut) {
        bbOut.put(offset, bvb.getOffByte());
    }

    protected final void writeThresholdedByte(
            int offset, ByteBuffer bbOut, VoxelBuffer<?> bbIn, VoxelBuffer<?> bbThrshld) {
        int val = bbIn.getInt(offset);
        int valThrshld = bbThrshld.getInt(offset);

        if (val >= valThrshld) {
            bbOut.put(offset, bvb.getOnByte());
        } else {
            bbOut.put(offset, bvb.getOffByte());
        }
    }
}
