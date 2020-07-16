/* (C)2020 */
package org.anchoranalysis.plugin.image.segment.watershed.encoding;

import java.util.Optional;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.buffer.SlidingBuffer;
import org.anchoranalysis.image.voxel.iterator.IterateVoxels;
import org.anchoranalysis.image.voxel.iterator.changed.ProcessVoxelNeighbor;
import org.anchoranalysis.image.voxel.iterator.changed.ProcessVoxelNeighborAbsoluteWithSlidingBuffer;
import org.anchoranalysis.image.voxel.iterator.changed.ProcessVoxelNeighborFactory;
import org.anchoranalysis.image.voxel.neighborhood.Neighborhood;
import org.anchoranalysis.image.voxel.neighborhood.NeighborhoodFactory;

public final class SteepestCalc {

    private static class PointTester
            extends ProcessVoxelNeighborAbsoluteWithSlidingBuffer<Integer> {

        private WatershedEncoding encoder;

        private int steepestDrctn;
        private int steepestVal;

        public PointTester(WatershedEncoding encoder, SlidingBuffer<?> rbb) {
            super(rbb);
            this.encoder = encoder;
        }

        @Override
        public void initSource(int sourceVal, int sourceOffsetXY) {
            super.initSource(sourceVal, sourceOffsetXY);
            this.steepestVal = sourceVal;
            this.steepestDrctn = WatershedEncoding.CODE_MINIMA;
        }

        @Override
        public boolean processPoint(int xChange, int yChange, int x1, int y1) {

            int gValNeighborhood = getInt(xChange, yChange);

            if (gValNeighborhood == sourceVal) {
                steepestDrctn = WatershedEncoding.CODE_PLATEAU;
                return true;
            }

            if (gValNeighborhood < steepestVal) {
                steepestVal = gValNeighborhood;
                steepestDrctn = encoder.encodeDirection(xChange, yChange, zChange);
                return true;
            }

            return false;
        }

        /** The steepest direction */
        @Override
        public Integer collectResult() {
            return steepestDrctn;
        }
    }

    private final boolean do3D;
    private final ProcessVoxelNeighbor<Integer> process;
    private final Neighborhood neighborhood;

    /**
     * @param rbb
     * @param encoder
     * @param do3D
     * @param bigNeighborhood iff true we use 8-Connectivity instead of 4, and 26-connectivity
     *     instead of 6 in 3D
     * @param mask
     */
    public SteepestCalc(
            SlidingBuffer<?> rbb,
            WatershedEncoding encoder,
            boolean do3D,
            boolean bigNeighborhood,
            Optional<ObjectMask> mask) {
        this.do3D = do3D;
        this.process =
                ProcessVoxelNeighborFactory.within(
                        mask, rbb.extent(), new PointTester(encoder, rbb));
        this.neighborhood = NeighborhoodFactory.of(bigNeighborhood);
    }

    // Calculates the steepest descent
    public int calcSteepestDescent(Point3i point, int val, int indxBuffer) {
        return IterateVoxels.callEachPointInNeighborhood(
                point, neighborhood, do3D, process, val, indxBuffer);
    }
}
