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

package org.anchoranalysis.plugin.image.segment.watershed.encoding;

import java.util.Optional;
import org.anchoranalysis.image.voxel.buffer.SlidingBuffer;
import org.anchoranalysis.image.voxel.iterator.neighbor.IterateVoxelsNeighbors;
import org.anchoranalysis.image.voxel.iterator.neighbor.ProcessVoxelNeighbor;
import org.anchoranalysis.image.voxel.iterator.neighbor.ProcessVoxelNeighborAbsoluteWithSlidingBuffer;
import org.anchoranalysis.image.voxel.iterator.neighbor.ProcessVoxelNeighborFactory;
import org.anchoranalysis.image.voxel.neighborhood.Neighborhood;
import org.anchoranalysis.image.voxel.neighborhood.NeighborhoodFactory;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.spatial.point.Point3i;

/** Calculates the steepest descent direction for voxels in a watershed segmentation. */
public final class Steepest {

    private static class PointEvaluator
            extends ProcessVoxelNeighborAbsoluteWithSlidingBuffer<Integer> {

        private final WatershedEncoding encoder;

        private int direction;
        private int value;

        public PointEvaluator(WatershedEncoding encoder, SlidingBuffer<?> buffer) {
            super(buffer);
            this.encoder = encoder;
        }

        @Override
        public void initSource(int sourceValue, int sourceOffsetXY) {
            super.initSource(sourceValue, sourceOffsetXY);
            this.value = sourceValue;
            this.direction = WatershedEncoding.CODE_MINIMA;
        }

        @Override
        public void processPoint(int xChange, int yChange, int x1, int y1) {

            int gValNeighborhood = getInt(xChange, yChange);

            if (gValNeighborhood == sourceValue) {
                direction = WatershedEncoding.CODE_PLATEAU;
            }

            if (gValNeighborhood < value) {
                value = gValNeighborhood;
                direction = encoder.encodeDirection(xChange, yChange, zChange);
            }
        }

        /** The steepest direction */
        @Override
        public Integer collectResult() {
            return direction;
        }
    }

    private final boolean do3D;
    private final ProcessVoxelNeighbor<Integer> process;
    private final Neighborhood neighborhood;

    /**
     * Creates a Steepest instance for a specific buffer.
     *
     * @param buffer the {@link SlidingBuffer} to process
     * @param encoder the {@link WatershedEncoding} to use for encoding directions
     * @param do3D whether to perform 3D processing
     * @param bigNeighborhood if true, use 8-Connectivity instead of 4 in 2D, and 26-connectivity
     *     instead of 6 in 3D, as per {@link NeighborhoodFactory}
     * @param objectMask an optional {@link ObjectMask} to restrict processing
     */
    public Steepest(
            SlidingBuffer<?> buffer,
            WatershedEncoding encoder,
            boolean do3D,
            boolean bigNeighborhood,
            Optional<ObjectMask> objectMask) {
        this.do3D = do3D;
        this.process =
                ProcessVoxelNeighborFactory.within(
                        objectMask, buffer.extent(), new PointEvaluator(encoder, buffer));
        this.neighborhood = NeighborhoodFactory.of(bigNeighborhood);
    }

    /**
     * Calculates the steepest descent direction for a given point.
     *
     * @param point the {@link Point3i} to calculate the steepest descent for
     * @param val the value at the given point
     * @param indxBuffer the buffer index for the given point
     * @return the encoded direction of steepest descent
     */
    public int steepestDescent(Point3i point, int val, int indxBuffer) {
        return IterateVoxelsNeighbors.callEachPointInNeighborhood(
                point, neighborhood, do3D, process, val, indxBuffer);
    }
}
