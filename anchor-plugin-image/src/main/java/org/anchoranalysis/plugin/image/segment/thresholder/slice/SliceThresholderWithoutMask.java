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

import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.spatial.box.Extent;

/** A slice thresholder that applies thresholding to the entire image without using a mask. */
public class SliceThresholderWithoutMask extends SliceThresholder {

    /**
     * Creates a new {@link SliceThresholderWithoutMask}.
     *
     * @param binaryValues the {@link BinaryValuesByte} to use for output
     */
    public SliceThresholderWithoutMask(BinaryValuesByte binaryValues) {
        super(binaryValues);
    }

    @Override
    public void segmentAll(
            Voxels<?> voxelsIn, Voxels<?> voxelsThreshold, Voxels<UnsignedByteBuffer> voxelsOut) {
        for (int z = 0; z < voxelsIn.extent().z(); z++) {
            segmentSlice(
                    voxelsIn.extent(),
                    voxelsIn.slice(z),
                    voxelsThreshold.slice(z),
                    voxelsOut.slice(z));
        }
    }

    /**
     * Segments a single slice of the image.
     *
     * @param extent the {@link Extent} of the slice
     * @param voxelsIn the input {@link VoxelBuffer}
     * @param voxelsThreshold the threshold {@link VoxelBuffer}
     * @param bufferOut the output {@link VoxelBuffer}
     */
    private void segmentSlice(
            Extent extent,
            VoxelBuffer<?> voxelsIn,
            VoxelBuffer<?> voxelsThreshold,
            VoxelBuffer<UnsignedByteBuffer> bufferOut) {
        UnsignedByteBuffer out = bufferOut.buffer();

        int offset = 0;
        for (int y = 0; y < extent.y(); y++) {
            for (int x = 0; x < extent.x(); x++) {
                writeThresholdedByte(offset, out, voxelsIn, voxelsThreshold);
                offset++;
            }
        }
    }
}
