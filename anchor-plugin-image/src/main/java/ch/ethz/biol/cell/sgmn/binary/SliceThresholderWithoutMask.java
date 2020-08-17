/*-
 * #%L
 * anchor-plugin-image
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

package ch.ethz.biol.cell.sgmn.binary;

import java.nio.ByteBuffer;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;

public class SliceThresholderWithoutMask extends SliceThresholder {

    public SliceThresholderWithoutMask(BinaryValuesByte bvb) {
        super(bvb);
    }

    @Override
    public void sgmnAll(Voxels<?> voxelsIn, Voxels<?> voxelsThrshld, Voxels<ByteBuffer> voxelsOut) {
        for (int z = 0; z < voxelsIn.extent().z(); z++) {
            sgmnSlice(
                    voxelsIn.extent(),
                    voxelsIn.slice(z),
                    voxelsThrshld.slice(z),
                    voxelsOut.slice(z));
        }
    }

    private void sgmnSlice(
            Extent extent,
            VoxelBuffer<?> voxelsIn,
            VoxelBuffer<?> voxelsThrshld,
            VoxelBuffer<ByteBuffer> bbOut) {
        ByteBuffer out = bbOut.buffer();

        int offset = 0;
        for (int y = 0; y < extent.y(); y++) {
            for (int x = 0; x < extent.x(); x++) {
                writeThresholdedByte(offset, out, voxelsIn, voxelsThrshld);
                offset++;
            }
        }
    }
}
