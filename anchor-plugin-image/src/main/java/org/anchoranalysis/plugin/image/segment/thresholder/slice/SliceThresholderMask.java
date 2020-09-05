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

import org.anchoranalysis.image.convert.UnsignedByteBuffer;
import org.anchoranalysis.core.geometry.ReadableTuple3i;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;

public class SliceThresholderMask extends SliceThresholder {

    private final boolean clearOutsideMask;
    private final ObjectMask object;
    private final ReadableTuple3i cornerMin;
    private final ReadableTuple3i cornerMax;

    public SliceThresholderMask(boolean clearOutsideMask, ObjectMask object, BinaryValuesByte bvb) {
        super(bvb);
        this.clearOutsideMask = clearOutsideMask;
        this.object = object;
        this.cornerMin = object.boundingBox().cornerMin();
        this.cornerMax = object.boundingBox().calculateCornerMax();
    }

    @Override
    public void segmentAll(
            Voxels<?> voxelsIn, Voxels<?> voxelsThrshld, Voxels<UnsignedByteBuffer> voxelsOut) {
        for (int z = cornerMin.z(); z <= cornerMax.z(); z++) {

            int relZ = z - cornerMin.z();

            sgmnSlice(
                    voxelsIn.extent(),
                    voxelsIn.slice(relZ),
                    voxelsThrshld.slice(relZ),
                    voxelsOut.slice(relZ),
                    object.voxels().slice(z),
                    object.binaryValuesByte());
        }
    }

    private void sgmnSlice(
            Extent extent,
            VoxelBuffer<?> voxelsIn,
            VoxelBuffer<?> voxelsThrshld,
            VoxelBuffer<UnsignedByteBuffer> voxelsOut,
            VoxelBuffer<UnsignedByteBuffer> voxelsMask,
            BinaryValuesByte bvbMask) {
        int offsetMask = 0;
        UnsignedByteBuffer out = voxelsOut.buffer();

        for (int y = cornerMin.y(); y <= cornerMax.y(); y++) {
            for (int x = cornerMin.x(); x <= cornerMax.x(); x++) {

                int offset = extent.offset(x, y);

                if (voxelsMask.buffer().getRaw(offsetMask++) == bvbMask.getOffByte()) {

                    if (clearOutsideMask) {
                        writeOffByte(offset, out);
                    }

                    continue;
                }

                writeThresholdedByte(offset, out, voxelsIn, voxelsThrshld);
            }
        }
    }
}
