/* (C)2020 */
package org.anchoranalysis.plugin.mpp.feature.bean.memo.ind;

import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputSingleMemo;
import org.anchoranalysis.anchor.mpp.pxlmark.VoxelizedMark;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;

// Size = Number of voxels
public final class Size extends FeatureSingleMemoRegion {

    @Override
    public double calc(SessionInput<FeatureInputSingleMemo> input) throws FeatureCalcException {

        VoxelizedMark pm = input.get().getPxlPartMemo().voxelized();

        VoxelStatistics pxlStats = pm.statisticsForAllSlices(0, getRegionID());

        return resolveVolume((double) pxlStats.size(), input.get().getResOptional());
    }
}
