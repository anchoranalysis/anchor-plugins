/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.voxelbox.pixelsforplane;

import ij.process.FloatProcessor;
import java.nio.FloatBuffer;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.voxel.box.pixelsforplane.PixelsForPlane;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.buffer.VoxelBufferFloat;

public class PixelsFromFloatProcessor implements PixelsForPlane<FloatBuffer> {

    private FloatProcessor bp;
    private Extent extent;

    public PixelsFromFloatProcessor(FloatProcessor fp) {
        super();
        this.bp = fp;
        this.extent = new Extent(fp.getWidth(), fp.getHeight(), 1);
    }

    @Override
    public VoxelBuffer<FloatBuffer> getPixelsForPlane(int z) {
        return VoxelBufferFloat.wrap((float[]) bp.getPixels());
    }

    @Override
    public void setPixelsForPlane(int z, VoxelBuffer<FloatBuffer> pixels) {
        assert (z == 0);
        bp.setPixels(pixels.buffer().array());
    }

    @Override
    public Extent extent() {
        return extent;
    }
}
