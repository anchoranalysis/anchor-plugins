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

import java.util.Optional;
import lombok.AllArgsConstructor;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.VoxelsUntyped;
import org.anchoranalysis.image.voxel.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.voxel.buffer.SlidingBuffer;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.image.voxel.factory.VoxelsFactory;
import org.anchoranalysis.image.voxel.iterator.IterateVoxelsAll;
import org.anchoranalysis.image.voxel.iterator.IterateVoxelsObjectMask;
import org.anchoranalysis.image.voxel.iterator.neighbor.IterateVoxelsNeighbors;
import org.anchoranalysis.image.voxel.iterator.neighbor.ProcessVoxelNeighbor;
import org.anchoranalysis.image.voxel.iterator.neighbor.ProcessVoxelNeighborFactory;
import org.anchoranalysis.image.voxel.iterator.process.voxelbuffer.ProcessVoxelBufferBinaryMixed;
import org.anchoranalysis.image.voxel.neighborhood.Neighborhood;
import org.anchoranalysis.image.voxel.neighborhood.NeighborhoodFactory;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.segment.watershed.encoding.PriorityQueueIndexRangeDownhill;
import org.anchoranalysis.spatial.box.Extent;
import org.anchoranalysis.spatial.point.Point3i;

/** Implements grayscale reconstruction using Robinson's algorithm. */
public class GrayscaleReconstructionRobinson extends GrayscaleReconstructionByErosion {

    /** The binary value representing an "on" state in the output. */
    private static final byte OUT_ON = BinaryValuesByte.getDefault().getOn();

    @Override
    public VoxelsUntyped reconstruction(
            VoxelsUntyped mask, VoxelsUntyped marker, Optional<ObjectMask> containingMask) {

        // We flip everything because what occurs is erosion by Dilation, whereas we want
        // reconstruction by Erosion
        marker.subtractFromMaxValue();
        mask.subtractFromMaxValue();

        VoxelsUntyped reconstructed =
                new VoxelsUntyped(reconstructionByDilation(mask, marker.any(), containingMask));
        reconstructed.subtractFromMaxValue();
        return reconstructed;
    }

    /**
     * markerForReconstruction is now in the same condition as the 'strong' condition in the
     * Robinson paper.
     *
     * <p>All pixels are either 0 or their final value (from channel).
     */
    private <T> Voxels<?> reconstructionByDilation(
            VoxelsUntyped mask, Voxels<T> marker, Optional<ObjectMask> containingMask) {

        // We use this to track if something has been finalized or not
        Voxels<UnsignedByteBuffer> voxelsFinalized =
                VoxelsFactory.getUnsignedByte().createInitialized(marker.extent());

        // TODO make more efficient
        // Find maximum value of markerVb.... we can probably get this elsewhere without having to
        // iterate the image again
        int maxValue = (int) marker.extract().voxelWithMaxIntensity();

        PriorityQueueIndexRangeDownhill<Point3i> queue =
                new PriorityQueueIndexRangeDownhill<>(maxValue);

        // TODO make more efficient
        // We put all non-zero pixels in our queue (these correspond to our seeds from our marker,
        // but let's iterate the image again
        //  for sake of keeping modularity

        VoxelProcessor<T> processor = new VoxelProcessor<>(queue, OUT_ON);

        if (containingMask.isPresent()) {
            IterateVoxelsObjectMask.withTwoMixedBuffers(
                    containingMask.get(), marker, voxelsFinalized, processor);
        } else {
            IterateVoxelsAll.withTwoMixedBuffers(marker, voxelsFinalized, processor);
        }

        readFromQueueUntilEmpty(queue, marker, mask.any(), voxelsFinalized, containingMask);

        return marker;
    }

    /**
     * Processes the priority queue until it's empty, updating the marker voxels.
     *
     * @param queue the {@link PriorityQueueIndexRangeDownhill} containing points to process
     * @param voxelsMarker the {@link Voxels} representing the marker
     * @param voxelsMask the {@link Voxels} representing the mask
     * @param voxelsFinalized the {@link Voxels} tracking finalized voxels
     * @param containingMask an optional {@link ObjectMask} to limit the processing area
     */
    private void readFromQueueUntilEmpty(
            PriorityQueueIndexRangeDownhill<Point3i> queue,
            Voxels<?> voxelsMarker,
            Voxels<?> voxelsMask,
            Voxels<UnsignedByteBuffer> voxelsFinalized,
            Optional<ObjectMask> containingMask) {

        Extent extent = voxelsMarker.extent();

        SlidingBuffer<?> bufferMarker = new SlidingBuffer<>(voxelsMarker);
        SlidingBuffer<?> bufferMask = new SlidingBuffer<>(voxelsMask);
        SlidingBuffer<UnsignedByteBuffer> bufferFinalized = new SlidingBuffer<>(voxelsFinalized);

        BinaryValuesByte binaryValuesFinalized = BinaryValuesByte.getDefault();

        ProcessVoxelNeighbor<?> process =
                ProcessVoxelNeighborFactory.within(
                        containingMask,
                        extent,
                        new PointProcessor(
                                bufferMarker,
                                bufferMask,
                                bufferFinalized,
                                queue,
                                binaryValuesFinalized));

        Neighborhood neighborhood = NeighborhoodFactory.of(false);
        boolean do3D = extent.z() > 1;

        for (int nextVal = queue.nextValue(); nextVal != -1; nextVal = queue.nextValue()) {

            Point3i point = queue.get();

            bufferMarker.seek(point.z());
            bufferMask.seek(point.z());
            bufferFinalized.seek(point.z());

            // We have a point, and a value
            // Now we iterate through the neighbors (but only if they haven't been finalised)
            // Makes sure that it includes its center point
            IterateVoxelsNeighbors.callEachPointInNeighborhood(
                    point, neighborhood, do3D, process, nextVal, extent.offsetSlice(point));
        }
    }

    /**
     * Processes voxels and adds them to the priority queue if they meet certain conditions.
     *
     * @param <T> the voxel data type
     */
    @AllArgsConstructor
    private static class VoxelProcessor<T>
            implements ProcessVoxelBufferBinaryMixed<T, UnsignedByteBuffer> {

        /** The priority queue to add points to. */
        private final PriorityQueueIndexRangeDownhill<Point3i> queue;

        /** The binary value representing an "on" state in the mask. */
        private final byte maskOn;

        @Override
        public void process(
                Point3i point, VoxelBuffer<T> buffer1, UnsignedByteBuffer buffer2, int offset) {
            int value = buffer1.getInt(offset);
            if (value != 0) {
                queue.put(point, value);
                buffer2.putRaw(offset, maskOn);
            }
        }
    }
}
