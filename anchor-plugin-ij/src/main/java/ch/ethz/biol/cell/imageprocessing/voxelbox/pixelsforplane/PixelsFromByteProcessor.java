/*-
 * #%L
 * anchor-plugin-ij
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

package ch.ethz.biol.cell.imageprocessing.voxelbox.pixelsforplane;

import ij.process.ByteProcessor;
import java.nio.ByteBuffer;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.buffer.VoxelBufferByte;
import org.anchoranalysis.image.voxel.pixelsforslice.PixelsForSlice;
import com.google.common.base.Preconditions;

public class PixelsFromByteProcessor implements PixelsForSlice<ByteBuffer> {

    private ByteProcessor bp;
    private Extent extent;

    public PixelsFromByteProcessor(ByteProcessor bp) {
        super();
        this.bp = bp;
        this.extent = new Extent(bp.getWidth(), bp.getHeight());
    }

    @Override
    public VoxelBuffer<ByteBuffer> slice(int z) {
        return VoxelBufferByte.wrap((byte[]) bp.getPixels());
    }

    @Override
    public void replaceSlice(int z, VoxelBuffer<ByteBuffer> pixels) {
        Preconditions.checkArgument(z==0);
        bp.setPixels(pixels.buffer().array());
    }

    @Override
    public Extent extent() {
        return extent;
    }
}
