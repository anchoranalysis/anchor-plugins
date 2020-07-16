/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.voxelbox.pixelsforplane;

import ij.process.ByteProcessor;
import java.nio.ByteBuffer;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.voxel.box.pixelsforplane.PixelsForPlane;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.buffer.VoxelBufferByte;

public class PixelsFromByteProcessor implements PixelsForPlane<ByteBuffer> {

    private ByteProcessor bp;
    private Extent extent;

    public PixelsFromByteProcessor(ByteProcessor bp) {
        super();
        this.bp = bp;
        this.extent = new Extent(bp.getWidth(), bp.getHeight(), 1);
    }

    @Override
    public VoxelBuffer<ByteBuffer> getPixelsForPlane(int z) {
        return VoxelBufferByte.wrap((byte[]) bp.getPixels());
    }

    @Override
    public void setPixelsForPlane(int z, VoxelBuffer<ByteBuffer> pixels) {
        assert (z == 0);
        bp.setPixels(pixels.buffer().array());
    }

    @Override
    public Extent extent() {
        return extent;
    }
}
