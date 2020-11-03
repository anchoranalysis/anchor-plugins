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

import org.anchoranalysis.image.voxel.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.voxel.buffer.SlidingBuffer;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.image.voxel.iterator.neighbor.ProcessVoxelNeighborAbsoluteWithSlidingBuffer;
import org.anchoranalysis.plugin.image.segment.watershed.encoding.PriorityQueueIndexRangeDownhill;
import org.anchoranalysis.spatial.point.Point3i;

/** There's no meaningful "result" value here, so we always return -1 */
class PointProcessor extends ProcessVoxelNeighborAbsoluteWithSlidingBuffer<Object> {

    private SlidingBuffer<?> slidingBufferMask;
    private SlidingBuffer<UnsignedByteBuffer> slidingBufferFinalized;
    private PriorityQueueIndexRangeDownhill<Point3i> queue;

    // Current ByteBuffer
    private VoxelBuffer<?> bufferMask;
    private VoxelBuffer<UnsignedByteBuffer> bufferFinalized;
    private int z = 0;

    private final BinaryValuesByte binaryValues;

    public PointProcessor(
            SlidingBuffer<?> slidingBufferMarker,
            SlidingBuffer<?> slidingBufferMask,
            SlidingBuffer<UnsignedByteBuffer> slidingBufferFinalized,
            PriorityQueueIndexRangeDownhill<Point3i> queue,
            BinaryValuesByte binaryValues) {
        super(slidingBufferMarker);
        this.slidingBufferFinalized = slidingBufferFinalized;
        this.slidingBufferMask = slidingBufferMask;
        this.queue = queue;
        this.binaryValues = binaryValues;
    }

    @Override
    public void notifyChangeZ(int zChange, int z) {
        super.notifyChangeZ(zChange, z);
        this.bufferMask = slidingBufferMask.bufferRelative(zChange);
        this.bufferFinalized = slidingBufferFinalized.bufferRelative(zChange);
        this.z = z;
    }

    @Override
    public boolean processPoint(int xChange, int yChange, int x1, int y1) {

        // We can replace with local index changes
        int index = changedOffset(xChange, yChange);

        // We see if it's been finalized or not
        byte value = bufferFinalized.buffer().getRaw(index);
        if (value == binaryValues.getOffByte()) {

            // get value from mask
            int maskVal = bufferMask.getInt(index);

            int valToWrite = Math.min(maskVal, sourceVal);

            // write this value to the output image
            putInt(index, valToWrite);

            // put the neighbor on the queue
            queue.put(new Point3i(x1, y1, z), valToWrite);

            // point as finalized
            bufferFinalized.buffer().putRaw(index, binaryValues.getOnByte());

            return true;
        }

        return false;
    }

    @Override
    public Integer collectResult() {
        // Arbitrary value as no result exists (instead buffers are changed during the iteration).
        return -1;
    }
}
