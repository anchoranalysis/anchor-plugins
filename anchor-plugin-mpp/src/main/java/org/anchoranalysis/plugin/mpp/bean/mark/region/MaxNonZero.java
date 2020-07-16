/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.mark.region;

import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.pxlmark.VoxelizedMark;
import org.anchoranalysis.bean.shared.relation.GreaterThanBean;
import org.anchoranalysis.bean.shared.relation.threshold.RelationToConstant;
import org.anchoranalysis.bean.shared.relation.threshold.RelationToThreshold;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;

public class MaxNonZero extends IndexedRegionBase {

    @Override
    protected VoxelStatistics createStatisticsFor(
            VoxelizedMark voxelizedMark, Mark mark, ImageDimensions dimensions)
            throws CreateException {

        RelationToThreshold nonZero = new RelationToConstant(new GreaterThanBean(), 0);

        long maxNonZero = -1;
        VoxelStatistics maxStats = null;

        for (int z = 0; z < voxelizedMark.getBoundingBox().extent().getZ(); z++) {
            VoxelStatistics stats = sliceStatisticsForRegion(voxelizedMark, z);

            Histogram h;
            try {
                h = stats.histogram();
            } catch (OperationFailedException e) {
                throw new CreateException(e);
            }

            long num = h.countThreshold(nonZero);

            if (num > maxNonZero) {
                maxNonZero = num;
                maxStats = stats;
            }
        }

        return maxStats;
    }
}
