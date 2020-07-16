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
package org.anchoranalysis.plugin.image.bean.object.segment.watershed.minima.grayscalereconstruction;

import java.nio.ByteBuffer;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.voxel.buffer.SlidingBuffer;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.iterator.changed.ProcessVoxelNeighborAbsoluteWithSlidingBuffer;
import org.anchoranalysis.plugin.image.segment.watershed.encoding.PriorityQueueIndexRangeDownhill;

/** There's no meaningful "result" value here, so we always return -1 */
class PointProcessor extends ProcessVoxelNeighborAbsoluteWithSlidingBuffer<Object> {

    private SlidingBuffer<?> sbMask;
    private SlidingBuffer<ByteBuffer> sbFinalized;
    private PriorityQueueIndexRangeDownhill<Point3i> queue;

    // Current ByteBuffer
    private VoxelBuffer<?> bbMask;
    private VoxelBuffer<ByteBuffer> bbFinalized;
    private int z = 0;

    private final BinaryValuesByte bv;

    public PointProcessor(
            SlidingBuffer<?> sbMarker,
            SlidingBuffer<?> sbMask,
            SlidingBuffer<ByteBuffer> sbFinalized,
            PriorityQueueIndexRangeDownhill<Point3i> queue,
            BinaryValuesByte bv) {
        super(sbMarker);
        assert (sbMarker.extent().equals(sbFinalized.extent()));
        assert (sbMarker.extent().equals(sbMask.extent()));
        this.sbFinalized = sbFinalized;
        this.sbMask = sbMask;
        this.queue = queue;
        this.bv = bv;
    }

    @Override
    public void notifyChangeZ(int zChange, int z) {
        super.notifyChangeZ(zChange, z);
        this.bbMask = sbMask.bufferRel(zChange);
        this.bbFinalized = sbFinalized.bufferRel(zChange);
        this.z = z;
    }

    @Override
    public boolean processPoint(int xChange, int yChange, int x1, int y1) {

        // We can replace with local index changes
        int index = changedOffset(xChange, yChange);

        // We see if it's been finalized or not
        byte b = bbFinalized.buffer().get(index);
        if (b == bv.getOffByte()) {

            // get value from mask
            int maskVal = bbMask.getInt(index);

            int valToWrite = Math.min(maskVal, sourceVal);

            // write this value to the output image
            putInt(index, valToWrite);

            // put the neighbor on the queue
            queue.put(new Point3i(x1, y1, z), valToWrite);

            // point as finalized
            bbFinalized.buffer().put(index, bv.getOnByte());

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
