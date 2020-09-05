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

package org.anchoranalysis.plugin.image.bean.object.segment.channel.watershed.minima.grayscalereconstruction;

import org.anchoranalysis.image.convert.UnsignedByteBuffer;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.voxel.buffer.SlidingBuffer;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.iterator.changed.ProcessVoxelNeighborAbsoluteWithSlidingBuffer;
import org.anchoranalysis.plugin.image.segment.watershed.encoding.PriorityQueueIndexRangeDownhill;

/** There's no meaningful "result" value here, so we always return -1 */
class PointProcessor extends ProcessVoxelNeighborAbsoluteWithSlidingBuffer<Object> {

    private SlidingBuffer<?> sbMask;
    private SlidingBuffer<UnsignedByteBuffer> sbFinalized;
    private PriorityQueueIndexRangeDownhill<Point3i> queue;

    // Current ByteBuffer
    private VoxelBuffer<?> bufferMask;
    private VoxelBuffer<UnsignedByteBuffer> bufferFinalized;
    private int z = 0;

    private final BinaryValuesByte bv;

    public PointProcessor(
            SlidingBuffer<?> sbMarker,
            SlidingBuffer<?> sbMask,
            SlidingBuffer<UnsignedByteBuffer> sbFinalized,
            PriorityQueueIndexRangeDownhill<Point3i> queue,
            BinaryValuesByte bv) {
        super(sbMarker);
        this.sbFinalized = sbFinalized;
        this.sbMask = sbMask;
        this.queue = queue;
        this.bv = bv;
    }

    @Override
    public void notifyChangeZ(int zChange, int z) {
        super.notifyChangeZ(zChange, z);
        this.bufferMask = sbMask.bufferRel(zChange);
        this.bufferFinalized = sbFinalized.bufferRel(zChange);
        this.z = z;
    }

    @Override
    public boolean processPoint(int xChange, int yChange, int x1, int y1) {

        // We can replace with local index changes
        int index = changedOffset(xChange, yChange);

        // We see if it's been finalized or not
        byte b = bufferFinalized.buffer().getRaw(index);
        if (b == bv.getOffByte()) {

            // get value from mask
            int maskVal = bufferMask.getInt(index);

            int valToWrite = Math.min(maskVal, sourceVal);

            // write this value to the output image
            putInt(index, valToWrite);

            // put the neighbor on the queue
            queue.put(new Point3i(x1, y1, z), valToWrite);

            // point as finalized
            bufferFinalized.buffer().putRaw(index, bv.getOnByte());

            return true;
        }

        return false;
    }

    @Override
    public Integer collectResult() {
        return -1; // Arbitrary value as no result exists (instead buffers are changed during the
        // iteration)
    }
}
