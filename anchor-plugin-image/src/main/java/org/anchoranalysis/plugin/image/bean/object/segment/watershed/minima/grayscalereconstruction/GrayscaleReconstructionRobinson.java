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

package org.anchoranalysis.plugin.image.bean.object.segment.watershed.minima.grayscalereconstruction;

import java.nio.ByteBuffer;
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
            VoxelsWrapper maskVb, VoxelsWrapper markerVb, Optional<ObjectMask> containingMask) {

        // We flip everything because what occurs is erosion by Dilation, whereas we want
        // reconstruction by Erosion
        markerVb.subtractFromMaxValue();
        maskVb.subtractFromMaxValue();

        VoxelsWrapper ret = reconstructionByDilation(maskVb, markerVb, containingMask);
        ret.subtractFromMaxValue();

        return ret;
    }

    // we now have a markerForReconstruction in the same condition as the 'strong' condition in the
    // Robison paper
    // all pixels are either 0 or their final value (from chnl)
    private VoxelsWrapper reconstructionByDilation(
            VoxelsWrapper maskVb, VoxelsWrapper markerVb, Optional<ObjectMask> containingMask) {

        // We use this to track if something has been finalized or not
        Voxels<ByteBuffer> voxelsFinalized =
                VoxelsFactory.getByte().createInitialized(markerVb.any().extent());

        // TODO make more efficient
        // Find maximum value of makerVb.... we can probably get this elsewhere without having to
        // iterate the image again
        int maxValue = markerVb.extract().voxelWithMaxIntensity();

        PriorityQueueIndexRangeDownhill<Point3i> queue =
                new PriorityQueueIndexRangeDownhill<>(maxValue);

        // TODO make more efficient
        // We put all non-zero pixels in our queue (these correspond to our seeds from our marker,
        // but let's iterate the image again
        //  for sake of keeping modularity
        if (containingMask.isPresent()) {
            populateQueueFromNonZeroPixelsMask(
                    queue, markerVb.any(), voxelsFinalized, containingMask.get());
        } else {
            populateQueueFromNonZeroPixels(queue, markerVb.any(), voxelsFinalized);
        }

        readFromQueueUntilEmpty(
                queue, markerVb.any(), maskVb.any(), voxelsFinalized, containingMask);

        return markerVb;
    }

    private void readFromQueueUntilEmpty(
            PriorityQueueIndexRangeDownhill<Point3i> queue,
            Voxels<?> markerVb,
            Voxels<?> maskVb,
            Voxels<ByteBuffer> voxelsFinalized,
            Optional<ObjectMask> containingMask) {

        Extent extent = markerVb.extent();

        SlidingBuffer<?> sbMarker = new SlidingBuffer<>(markerVb);
        SlidingBuffer<?> sbMask = new SlidingBuffer<>(maskVb);
        SlidingBuffer<ByteBuffer> sbFinalized = new SlidingBuffer<>(voxelsFinalized);

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
            Voxels<ByteBuffer> voxelsFinalized) {

        byte maskOn = BinaryValuesByte.getDefault().getOnByte();

        Extent e = voxels.extent();
        for (int z = 0; z < e.z(); z++) {

            VoxelBuffer<?> bb = voxels.slice(z);
            ByteBuffer bbFinalized = voxelsFinalized.sliceBuffer(z);

            int offset = 0;
            for (int y = 0; y < e.y(); y++) {
                for (int x = 0; x < e.x(); x++) {

                    {
                        int val = bb.getInt(offset);
                        if (val != 0) {
                            queue.put(new Point3i(x, y, z), val);
                            bbFinalized.put(offset, maskOn);
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
            Voxels<ByteBuffer> voxelsFinalized,
            ObjectMask containingMask) {

        ReadableTuple3i crnrpointMin = containingMask.boundingBox().cornerMin();
        ReadableTuple3i crnrpointMax = containingMask.boundingBox().calculateCornerMax();

        byte maskOn = containingMask.binaryValuesByte().getOnByte();

        Extent e = voxels.extent();
        for (int z = crnrpointMin.z(); z <= crnrpointMax.z(); z++) {

            VoxelBuffer<?> bb = voxels.slice(z);
            ByteBuffer bbFinalized = voxelsFinalized.sliceBuffer(z);
            ByteBuffer bbMask = containingMask.sliceBufferGlobal(z);

            int offset = 0;
            for (int y = crnrpointMin.y(); y <= crnrpointMax.y(); y++) {
                for (int x = crnrpointMin.x(); x <= crnrpointMax.x(); x++) {
                    if (bbMask.get(offset) == maskOn) {

                        int offsetGlobal = e.offset(x, y);

                        {
                            int val = bb.getInt(offsetGlobal);
                            if (val != 0) {
                                queue.put(new Point3i(x, y, z), val);
                                bbFinalized.put(offsetGlobal, maskOn);
                            }
                        }
                    }

                    offset++;
                }
            }
        }
    }
}
