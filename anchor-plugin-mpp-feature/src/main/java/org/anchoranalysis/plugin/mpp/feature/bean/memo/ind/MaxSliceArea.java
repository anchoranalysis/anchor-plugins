/* (C)2020 */
package org.anchoranalysis.plugin.mpp.feature.bean.memo.ind;

import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputSingleMemo;
import org.anchoranalysis.anchor.mpp.pxlmark.VoxelizedMark;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;

// Returns the maximum area of each slice
public final class MaxSliceArea extends FeatureSingleMemoRegion {

    @Override
    public double calc(SessionInput<FeatureInputSingleMemo> input) throws FeatureCalcException {

        VoxelizedMark pm = input.get().getPxlPartMemo().voxelized();

        double maxSliceSizeVoxels = calcMaxSliceSize(pm);

        double retVal = resolveArea(maxSliceSizeVoxels, input.get().getResOptional());

        getLogger().messageLogger().logFormatted("MaxSliceArea = %f\n", retVal);
        return retVal;
    }

    private long calcMaxSliceSize(VoxelizedMark pm) {

        long max = 0;
        for (int z = 0; z < pm.getVoxelBox().extent().getZ(); z++) {

            VoxelStatistics pxlStats = pm.statisticsFor(0, getRegionID(), z);

            long size = pxlStats.size();

            if (size > max) {
                max = size;
            }
        }
        return max;
    }
}
