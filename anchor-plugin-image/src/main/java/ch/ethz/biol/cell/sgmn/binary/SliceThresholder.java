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
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;

public abstract class SliceThresholder {

    private final BinaryValuesByte bvb;

    public SliceThresholder(BinaryValuesByte bvb) {
        super();
        this.bvb = bvb;
    }

    public abstract void sgmnAll(
            Voxels<?> voxelsIn, Voxels<?> voxelsThrshld, Voxels<ByteBuffer> voxelsOut);

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
