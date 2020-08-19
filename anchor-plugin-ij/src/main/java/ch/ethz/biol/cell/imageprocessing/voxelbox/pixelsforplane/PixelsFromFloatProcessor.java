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

import ij.process.FloatProcessor;
import java.nio.FloatBuffer;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.buffer.VoxelBufferFloat;
import org.anchoranalysis.image.voxel.pixelsforslice.PixelsForSlice;
import com.google.common.base.Preconditions;

public class PixelsFromFloatProcessor implements PixelsForSlice<FloatBuffer> {

    private FloatProcessor bp;
    private Extent extent;

    public PixelsFromFloatProcessor(FloatProcessor fp) {
        super();
        this.bp = fp;
        this.extent = new Extent(fp.getWidth(), fp.getHeight());
    }

    @Override
    public VoxelBuffer<FloatBuffer> slice(int z) {
        return VoxelBufferFloat.wrap((float[]) bp.getPixels());
    }

    @Override
    public void replaceSlice(int z, VoxelBuffer<FloatBuffer> pixels) {
        Preconditions.checkArgument(z==0);
        bp.setPixels(pixels.buffer().array());
    }

    @Override
    public Extent extent() {
        return extent;
    }
}
