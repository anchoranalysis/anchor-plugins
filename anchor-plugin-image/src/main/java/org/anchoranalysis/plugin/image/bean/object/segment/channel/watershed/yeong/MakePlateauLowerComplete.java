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

package org.anchoranalysis.plugin.image.bean.object.segment.channel.watershed.yeong;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.image.voxel.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.image.voxel.iterator.neighbor.IterateVoxelsNeighbors;
import org.anchoranalysis.image.voxel.iterator.neighbor.ProcessChangedPointAbsoluteMasked;
import org.anchoranalysis.image.voxel.iterator.neighbor.ProcessVoxelNeighbor;
import org.anchoranalysis.image.voxel.iterator.neighbor.ProcessVoxelNeighborFactory;
import org.anchoranalysis.image.voxel.neighborhood.Neighborhood;
import org.anchoranalysis.image.voxel.neighborhood.NeighborhoodFactory;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.segment.watershed.encoding.EncodedVoxels;
import org.anchoranalysis.spatial.point.Point3i;

class MakePlateauLowerComplete {

    private static class PointEvaluator
            implements ProcessChangedPointAbsoluteMasked<List<Point3i>> {

        private final EncodedVoxels matS;

        private final List<Point3i> foundPoints = new ArrayList<>();

        private int zChange;
        private UnsignedByteBuffer buffer;
        private int z1;
        private final byte maskValueOff;

        public PointEvaluator(EncodedVoxels matS, BinaryValuesByte binaryValues) {
            this.matS = matS;
            this.maskValueOff = binaryValues.getOff();
        }

        @Override
        public void initSource(int sourceValue, int sourceOffsetXY) {
            foundPoints.clear();
        }

        @Override
        public void notifyChangeZ(int zChange, int z1, UnsignedByteBuffer objectMaskBuffer) {
            this.buffer = objectMaskBuffer;
            this.zChange = zChange;
            this.z1 = z1;
        }

        @Override
        public void processPoint(int xChange, int yChange, int x, int y, int objectMaskOffset) {

            Point3i pointRel = new Point3i(x, y, z1);
            foundPoints.add(pointRel);
            buffer.putRaw(objectMaskOffset, maskValueOff);

            // We point this value in the direction opposite to which it came
            matS.setPointDirection(pointRel, xChange * -1, yChange * -1, zChange * -1);
        }

        @Override
        public List<Point3i> collectResult() {
            return foundPoints;
        }
    }

    private EqualVoxelsPlateau plateau;
    private boolean do3D;

    public MakePlateauLowerComplete(EqualVoxelsPlateau plateau, boolean do3D) {
        super();
        this.plateau = plateau;
        this.do3D = do3D;
    }

    public void makeBufferLowerCompleteForPlateau(
            EncodedVoxels matS, Optional<MinimaStore> minimaStore) {

        assert (plateau.hasPoints());
        if (plateau.isOnlyEdge()) {
            pointEdgeToNeighboring(matS);
        } else if (plateau.isOnlyInner()) {
            // EVERYTHING COLLECTIVELY IS A LOCAL MINIMA
            // We pick one pixel to use as an index, and pointing the rest of the pixels
            //  of them towards it

            assert plateau.getPointsInner().size() >= 2;

            matS.pointListAtFirstPoint(plateau.getPointsInner());

            if (minimaStore.isPresent()) {
                minimaStore.get().add(plateau.getPointsInner());
            }
        } else {

            // IF IT'S MIXED...

            pointEdgeToNeighboring(matS);
            pointInnerToEdge(matS);
        }
    }

    private void pointEdgeToNeighboring(EncodedVoxels matS) {
        // We set them all to their neighboring points
        for (PointWithNeighbor pointNeighbor : plateau.getPointsEdge()) {
            matS.setPoint(pointNeighbor.getPoint(), pointNeighbor.getNeighborIndex());
        }
    }

    private void pointInnerToEdge(EncodedVoxels matS) {
        // Iterate through each edge pixel, and look for neighboring points in the Inner pixels
        //   for any such point, point towards the edge pixel, and move to the new edge list
        List<Point3i> searchPoints = plateau.pointsEdge();

        try {
            // We create an object-mask from the list of points
            ObjectMask object = CreateObjectFromPoints.create(plateau.getPointsInner());
            Neighborhood neighborhood = NeighborhoodFactory.of(true);

            ProcessVoxelNeighbor<List<Point3i>> process =
                    ProcessVoxelNeighborFactory.withinMask(
                            object, new PointEvaluator(matS, object.binaryValuesByte()));

            while (!searchPoints.isEmpty()) {
                searchPoints = findPointsFor(searchPoints, neighborhood, process);
            }

        } catch (CreateException e) {
            // the only exception possible should be when there are 0 pixels
            assert false;
        }
    }

    private List<Point3i> findPointsFor(
            List<Point3i> points,
            Neighborhood neighborhood,
            ProcessVoxelNeighbor<List<Point3i>> process) {

        List<Point3i> foundPoints = new ArrayList<>();

        // We iterate through all the search points
        for (Point3i point : points) {

            foundPoints.addAll(
                    IterateVoxelsNeighbors.callEachPointInNeighborhood(
                            point,
                            neighborhood,
                            do3D,
                            process,
                            -1, // The -1 value are arbitrary, as it will be ignored
                            -1 // The -1 value are arbitrary, as it will be ignored
                            ));
        }
        return foundPoints;
    }
}
