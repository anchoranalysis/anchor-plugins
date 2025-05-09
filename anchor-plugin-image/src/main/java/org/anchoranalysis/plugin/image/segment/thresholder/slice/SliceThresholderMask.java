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
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.spatial.box.Extent;
import org.anchoranalysis.spatial.point.ReadableTuple3i;

/** A slice thresholder that applies thresholding within a mask region. */
public class SliceThresholderMask extends SliceThresholder {

    /** Whether to clear voxels outside the mask. */
    private final boolean clearOutsideMask;

    /** The object mask defining the region for thresholding. */
    private final ObjectMask object;

    /** The minimum corner of the object's bounding box. */
    private final ReadableTuple3i cornerMin;

    /** The maximum corner of the object's bounding box. */
    private final ReadableTuple3i cornerMax;

    /**
     * Creates a new {@link SliceThresholderMask}.
     *
     * @param clearOutsideMask whether to clear voxels outside the mask
     * @param object the {@link ObjectMask} defining the region for thresholding
     * @param binaryValues the {@link BinaryValuesByte} to use for output
     */
    public SliceThresholderMask(
            boolean clearOutsideMask, ObjectMask object, BinaryValuesByte binaryValues) {
        super(binaryValues);
        this.clearOutsideMask = clearOutsideMask;
        this.object = object;
        this.cornerMin = object.boundingBox().cornerMin();
        this.cornerMax = object.boundingBox().calculateCornerMaxInclusive();
    }

    @Override
    public void segmentAll(
            Voxels<?> voxelsIn, Voxels<?> voxelsThreshold, Voxels<UnsignedByteBuffer> voxelsOut) {
        for (int z = cornerMin.z(); z <= cornerMax.z(); z++) {

            int relZ = z - cornerMin.z();

            sgmnSlice(
                    voxelsIn.extent(),
                    voxelsIn.slice(relZ),
                    voxelsThreshold.slice(relZ),
                    voxelsOut.slice(relZ),
                    object.voxels().slice(z),
                    object.binaryValuesByte());
        }
    }

    /**
     * Segments a single slice of the image.
     *
     * @param extent the {@link Extent} of the slice
     * @param voxelsIn the input {@link VoxelBuffer}
     * @param voxelsThreshold the threshold {@link VoxelBuffer}
     * @param voxelsOut the output {@link VoxelBuffer}
     * @param voxelsMask the mask {@link VoxelBuffer}
     * @param binaryValuesMask the {@link BinaryValuesByte} for the mask
     */
    private void sgmnSlice(
            Extent extent,
            VoxelBuffer<?> voxelsIn,
            VoxelBuffer<?> voxelsThreshold,
            VoxelBuffer<UnsignedByteBuffer> voxelsOut,
            VoxelBuffer<UnsignedByteBuffer> voxelsMask,
            BinaryValuesByte binaryValuesMask) {
        int offsetMask = 0;
        UnsignedByteBuffer out = voxelsOut.buffer();

        for (int y = cornerMin.y(); y <= cornerMax.y(); y++) {
            for (int x = cornerMin.x(); x <= cornerMax.x(); x++) {

                int offset = extent.offset(x, y);

                if (voxelsMask.buffer().getRaw(offsetMask++) == binaryValuesMask.getOff()) {

                    if (clearOutsideMask) {
                        writeOffByte(offset, out);
                    }

                    continue;
                }

                writeThresholdedByte(offset, out, voxelsIn, voxelsThreshold);
            }
        }
    }
}
