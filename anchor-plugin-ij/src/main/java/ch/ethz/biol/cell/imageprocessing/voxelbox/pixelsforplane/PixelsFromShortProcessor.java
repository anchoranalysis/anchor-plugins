/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.voxelbox.pixelsforplane;

import ij.process.ShortProcessor;
import java.nio.ShortBuffer;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.voxel.box.pixelsforplane.PixelsForPlane;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.buffer.VoxelBufferShort;

public class PixelsFromShortProcessor implements PixelsForPlane<ShortBuffer> {

    private ShortProcessor processor;
    private Extent extent;

    public PixelsFromShortProcessor(ShortProcessor processor) {
        super();
        this.processor = processor;
        this.extent = new Extent(processor.getWidth(), processor.getHeight(), 1);
    }

    @Override
    public VoxelBuffer<ShortBuffer> getPixelsForPlane(int z) {
        return VoxelBufferShort.wrap((short[]) processor.getPixels());
    }

    @Override
    public void setPixelsForPlane(int z, VoxelBuffer<ShortBuffer> pixels) {
        assert (z == 0);
        processor.setPixels(pixels.buffer().array());
    }

    @Override
    public Extent extent() {
        return extent;
    }
}
