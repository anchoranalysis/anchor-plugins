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

package org.anchoranalysis.plugin.image.bean.object.segment.watershed.yeong;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.buffer.SlidingBuffer;
import org.anchoranalysis.image.voxel.iterator.IterateVoxels;
import org.anchoranalysis.image.voxel.iterator.changed.ProcessVoxelNeighbor;
import org.anchoranalysis.image.voxel.iterator.changed.ProcessVoxelNeighborAbsoluteWithSlidingBuffer;
import org.anchoranalysis.image.voxel.iterator.changed.ProcessVoxelNeighborFactory;
import org.anchoranalysis.image.voxel.neighborhood.Neighborhood;
import org.anchoranalysis.image.voxel.neighborhood.NeighborhoodFactory;
import org.anchoranalysis.plugin.image.segment.watershed.encoding.EncodedIntBuffer;
import org.anchoranalysis.plugin.image.segment.watershed.encoding.EncodedVoxelBox;

final class FindEqualVoxels {

    private static class PointTester
            extends ProcessVoxelNeighborAbsoluteWithSlidingBuffer<Optional<Integer>> {

        private Deque<Point3i> stack;

        private EncodedVoxelBox matS;

        // Arguments for each point
        private int lowestNeighborVal;
        private int lowestNeighborIndex = -1;

        private EncodedIntBuffer bufS;
        private int z1;

        public PointTester(Deque<Point3i> stack, SlidingBuffer<?> rbb, EncodedVoxelBox matS) {
            super(rbb);
            this.stack = stack;
            this.matS = matS;
        }

        @Override
        public void initSource(int sourceVal, int sourceOffsetXY) {
            super.initSource(sourceVal, sourceOffsetXY);
            this.lowestNeighborVal = sourceVal;
            this.lowestNeighborIndex = -1;
        }

        /** The lowestNeighborIndex if it exists */
        @Override
        public Optional<Integer> collectResult() {
            if (lowestNeighborIndex != -1) {
                return Optional.of(lowestNeighborIndex);
            } else {
                return Optional.empty();
            }
        }

        @Override
        public void notifyChangeZ(int zChange, int z1) {
            super.notifyChangeZ(zChange, z1);
            bufS = matS.getPixelsForPlane(z1);
            this.z1 = z1;
        }

        @Override
        public boolean processPoint(int xChange, int yChange, int x1, int y1) {

            int offset = changedOffset(xChange, yChange);
            int valPoint = getInt(offset);

            // If we already have a connected component ID as a neighbor, it must because
            //   we have imposed seeds.  So we always point towards it, irrespective of
            //   its value
            if (bufS.isConnectedComponentID(offset)) {

                if (lowestNeighborIndex == -1) {
                    // We take anything
                    lowestNeighborVal = valPoint;
                    lowestNeighborIndex =
                            matS.getEncoding().encodeDirection(xChange, yChange, zChange);
                    return false;
                } else {
                    if (valPoint < lowestNeighborVal) {
                        lowestNeighborVal = valPoint;
                        lowestNeighborIndex =
                                matS.getEncoding().encodeDirection(xChange, yChange, zChange);
                    }
                    return false;
                }
            }

            if (valPoint == sourceVal) {
                stack.push(new Point3i(x1, y1, z1));
                return true;
            } else {
                // We test if the neighbor is less
                // NB we also force a check that it's less than the value to find, as this value
                //   might have been forced up by the connected component
                if (valPoint < sourceVal && valPoint < lowestNeighborVal) {
                    lowestNeighborVal = valPoint;
                    lowestNeighborIndex =
                            matS.getEncoding().encodeDirection(xChange, yChange, zChange);
                }
                return false;
            }
        }
    }

    private final VoxelBox<?> bufferValuesToFindEqual;
    private final EncodedVoxelBox matS;
    private final boolean do3D;
    private final Optional<ObjectMask> mask;

    public FindEqualVoxels(
            VoxelBox<?> bufferValuesToFindEqual,
            EncodedVoxelBox temporaryMarkVisitedBuffer,
            boolean do3D,
            Optional<ObjectMask> mask) {
        this.bufferValuesToFindEqual = bufferValuesToFindEqual;
        this.matS = temporaryMarkVisitedBuffer;
        this.do3D = do3D;
        this.mask = mask;
    }

    public EqualVoxelsPlateau createPlateau(Point3i point) {

        EqualVoxelsPlateau plateau = new EqualVoxelsPlateau();

        int valToFind =
                bufferValuesToFindEqual
                        .getPixelsForPlane(point.getZ())
                        .getInt(bufferValuesToFindEqual.extent().offsetSlice(point));

        SlidingBuffer<?> rbb = new SlidingBuffer<>(bufferValuesToFindEqual);

        {
            Deque<Point3i> stack = new LinkedList<>();
            stack.push(point);
            processStack(stack, rbb, plateau, valToFind);
        }

        assert (!plateau.hasNullItems());

        return plateau;
    }

    private void processStack(
            Deque<Point3i> stack,
            SlidingBuffer<?> slidingBuffer,
            EqualVoxelsPlateau plateau,
            int valToFind) {

        ProcessVoxelNeighbor<Optional<Integer>> process =
                ProcessVoxelNeighborFactory.within(
                        mask, slidingBuffer.extent(), new PointTester(stack, slidingBuffer, matS));

        Neighborhood neighborhood = NeighborhoodFactory.of(true);

        while (!stack.isEmpty()) {
            Point3i point = stack.pop();

            // If we've already visited this point, we skip it
            EncodedIntBuffer bbVisited = matS.getPixelsForPlane(point.getZ());
            int offset = slidingBuffer.extent().offsetSlice(point);
            if (bbVisited.isTemporary(offset)) {
                continue;
            }

            slidingBuffer.seek(point.getZ());

            Optional<Integer> lowestNeighborIndex =
                    IterateVoxels.callEachPointInNeighborhood(
                            point, neighborhood, do3D, process, valToFind, offset);

            bbVisited.markAsTemporary(offset);

            if (lowestNeighborIndex.isPresent()) {
                plateau.addEdge(point, lowestNeighborIndex.get());
            } else {
                plateau.addInner(point);
            }
        }
    }

    public boolean isDo3D() {
        return do3D;
    }
}
