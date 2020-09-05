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
import java.util.Optional;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.geometry.ReadableTuple3i;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.VoxelsWrapper;
import org.anchoranalysis.image.voxel.buffer.SlidingBuffer;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.factory.VoxelsFactory;
import org.anchoranalysis.image.voxel.iterator.IterateVoxels;
import org.anchoranalysis.image.voxel.iterator.changed.ProcessVoxelNeighbor;
import org.anchoranalysis.image.voxel.iterator.changed.ProcessVoxelNeighborFactory;
import org.anchoranalysis.image.voxel.neighborhood.Neighborhood;
import org.anchoranalysis.image.voxel.neighborhood.NeighborhoodFactory;
import org.anchoranalysis.plugin.image.segment.watershed.encoding.PriorityQueueIndexRangeDownhill;

public class GrayscaleReconstructionRobinson extends GrayscaleReconstructionByErosion {

    @Override
    public VoxelsWrapper reconstruction(
            VoxelsWrapper mask, VoxelsWrapper marker, Optional<ObjectMask> containingMask) {

        // We flip everything because what occurs is erosion by Dilation, whereas we want
        // reconstruction by Erosion
        marker.subtractFromMaxValue();
        mask.subtractFromMaxValue();

        VoxelsWrapper reconstructed = reconstructionByDilation(mask, marker, containingMask);
        reconstructed.subtractFromMaxValue();
        return reconstructed;
    }

    // we now have a markerForReconstruction in the same condition as the 'strong' condition in the
    // Robison paper
    // all pixels are either 0 or their final value (from channel)
    private VoxelsWrapper reconstructionByDilation(
            VoxelsWrapper mask, VoxelsWrapper marker, Optional<ObjectMask> containingMask) {

        // We use this to track if something has been finalized or not
        Voxels<UnsignedByteBuffer> voxelsFinalized =
                VoxelsFactory.getByte().createInitialized(marker.any().extent());

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
        if (containingMask.isPresent()) {
            populateQueueFromNonZeroPixelsMask(
                    queue, marker.any(), voxelsFinalized, containingMask.get());
        } else {
            populateQueueFromNonZeroPixels(queue, marker.any(), voxelsFinalized);
        }

        readFromQueueUntilEmpty(queue, marker.any(), mask.any(), voxelsFinalized, containingMask);

        return marker;
    }

    private void readFromQueueUntilEmpty(
            PriorityQueueIndexRangeDownhill<Point3i> queue,
            Voxels<?> markerVb,
            Voxels<?> maskVb,
            Voxels<UnsignedByteBuffer> voxelsFinalized,
            Optional<ObjectMask> containingMask) {

        Extent extent = markerVb.extent();

        SlidingBuffer<?> sbMarker = new SlidingBuffer<>(markerVb);
        SlidingBuffer<?> sbMask = new SlidingBuffer<>(maskVb);
        SlidingBuffer<UnsignedByteBuffer> sbFinalized = new SlidingBuffer<>(voxelsFinalized);

        BinaryValuesByte bvFinalized = BinaryValuesByte.getDefault();

        ProcessVoxelNeighbor<?> process =
                ProcessVoxelNeighborFactory.within(
                        containingMask,
                        extent,
                        new PointProcessor(sbMarker, sbMask, sbFinalized, queue, bvFinalized));

        Neighborhood neighborhood = NeighborhoodFactory.of(false);
        boolean do3D = extent.z() > 1;

        for (int nextVal = queue.nextValue(); nextVal != -1; nextVal = queue.nextValue()) {

            Point3i point = queue.get();

            sbMarker.seek(point.z());
            sbMask.seek(point.z());
            sbFinalized.seek(point.z());

            // We have a point, and a value
            // Now we iterate through the neighbors (but only if they haven't been finalised)
            // Makes sure that it includes its center point
            IterateVoxels.callEachPointInNeighborhood(
                    point, neighborhood, do3D, process, nextVal, extent.offsetSlice(point));
        }
    }

    private void populateQueueFromNonZeroPixels(
            PriorityQueueIndexRangeDownhill<Point3i> queue,
            Voxels<?> voxels,
            Voxels<UnsignedByteBuffer> voxelsFinalized) {

        byte maskOn = BinaryValuesByte.getDefault().getOnByte();

        Extent e = voxels.extent();
        for (int z = 0; z < e.z(); z++) {

            VoxelBuffer<?> buffer = voxels.slice(z);
            UnsignedByteBuffer bufferFinalized = voxelsFinalized.sliceBuffer(z);

            int offset = 0;
            for (int y = 0; y < e.y(); y++) {
                for (int x = 0; x < e.x(); x++) {

                    {
                        int val = buffer.getInt(offset);
                        if (val != 0) {
                            queue.put(new Point3i(x, y, z), val);
                            bufferFinalized.putRaw(offset, maskOn);
                        }
                    }

                    offset++;
                }
            }
        }
    }

    private void populateQueueFromNonZeroPixelsMask(
            PriorityQueueIndexRangeDownhill<Point3i> queue,
            Voxels<?> voxels,
            Voxels<UnsignedByteBuffer> voxelsFinalized,
            ObjectMask containingMask) {

        ReadableTuple3i cornerMin = containingMask.boundingBox().cornerMin();
        ReadableTuple3i cornerMax = containingMask.boundingBox().calculateCornerMax();

        byte maskOn = containingMask.binaryValuesByte().getOnByte();

        Extent e = voxels.extent();
        for (int z = cornerMin.z(); z <= cornerMax.z(); z++) {

            VoxelBuffer<?> buffer = voxels.slice(z);
            UnsignedByteBuffer bufferFinalized = voxelsFinalized.sliceBuffer(z);
            UnsignedByteBuffer bufferMask = containingMask.sliceBufferGlobal(z);

            int offset = 0;
            for (int y = cornerMin.y(); y <= cornerMax.y(); y++) {
                for (int x = cornerMin.x(); x <= cornerMax.x(); x++) {
                    if (bufferMask.getRaw(offset) == maskOn) {

                        int offsetGlobal = e.offset(x, y);

                        {
                            int val = buffer.getInt(offsetGlobal);
                            if (val != 0) {
                                queue.put(new Point3i(x, y, z), val);
                                bufferFinalized.putRaw(offsetGlobal, maskOn);
                            }
                        }
                    }

                    offset++;
                }
            }
        }
    }
}
