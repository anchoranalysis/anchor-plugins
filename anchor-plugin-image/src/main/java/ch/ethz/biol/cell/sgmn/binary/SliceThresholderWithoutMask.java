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
/* (C)2020 */
package ch.ethz.biol.cell.sgmn.binary;

import java.nio.ByteBuffer;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;

public class SliceThresholderWithoutMask extends SliceThresholder {

    public SliceThresholderWithoutMask(BinaryValuesByte bvb) {
        super(bvb);
    }

    @Override
    public void sgmnAll(
            VoxelBox<?> voxelBoxIn, VoxelBox<?> vbThrshld, VoxelBox<ByteBuffer> voxelBoxOut) {
        for (int z = 0; z < voxelBoxIn.extent().getZ(); z++) {
            sgmnSlice(
                    voxelBoxIn.extent(),
                    voxelBoxIn.getPixelsForPlane(z),
                    vbThrshld.getPixelsForPlane(z),
                    voxelBoxOut.getPixelsForPlane(z));
        }
    }

    private void sgmnSlice(
            Extent extent,
            VoxelBuffer<?> vbIn,
            VoxelBuffer<?> vbThrshld,
            VoxelBuffer<ByteBuffer> bbOut) {
        ByteBuffer out = bbOut.buffer();

        int offset = 0;
        for (int y = 0; y < extent.getY(); y++) {
            for (int x = 0; x < extent.getX(); x++) {
                writeThresholdedByte(offset, out, vbIn, vbThrshld);
                offset++;
            }
        }
    }
}
