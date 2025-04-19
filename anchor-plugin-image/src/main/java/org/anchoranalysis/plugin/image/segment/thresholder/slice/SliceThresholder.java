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

package org.anchoranalysis.plugin.image.segment.thresholder.slice;

import lombok.AllArgsConstructor;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;

/**
 * Segmenting a z-{@link Stack} slice-by-slice.
 *
 * @author Owen Feehan
 */
@AllArgsConstructor
public abstract class SliceThresholder {

    /** The binary values to use for the output. */
    private final BinaryValuesByte binaryValuesByte;

    /**
     * Segments all slices in the input voxels.
     *
     * @param voxelsIn the input {@link Voxels}
     * @param voxelsThreshold the threshold {@link Voxels}
     * @param voxelsOut the output {@link Voxels}
     */
    public abstract void segmentAll(
            Voxels<?> voxelsIn, Voxels<?> voxelsThreshold, Voxels<UnsignedByteBuffer> voxelsOut);

    /**
     * Writes the 'off' binary value to the output buffer at the specified offset.
     *
     * @param offset the offset in the buffer
     * @param bufferOut the output {@link UnsignedByteBuffer}
     */
    protected final void writeOffByte(int offset, UnsignedByteBuffer bufferOut) {
        bufferOut.putRaw(offset, binaryValuesByte.getOff());
    }

    /**
     * Writes a thresholded value to the output buffer based on the input and threshold buffers.
     *
     * @param offset the offset in the buffers
     * @param bufferOut the output {@link UnsignedByteBuffer}
     * @param bufferIn the input {@link VoxelBuffer}
     * @param bufferThreshold the threshold {@link VoxelBuffer}
     */
    protected final void writeThresholdedByte(
            int offset,
            UnsignedByteBuffer bufferOut,
            VoxelBuffer<?> bufferIn,
            VoxelBuffer<?> bufferThreshold) {
        int val = bufferIn.getInt(offset);
        int valThrshld = bufferThreshold.getInt(offset);

        if (val >= valThrshld) {
            bufferOut.putRaw(offset, binaryValuesByte.getOn());
        } else {
            bufferOut.putRaw(offset, binaryValuesByte.getOff());
        }
    }
}
