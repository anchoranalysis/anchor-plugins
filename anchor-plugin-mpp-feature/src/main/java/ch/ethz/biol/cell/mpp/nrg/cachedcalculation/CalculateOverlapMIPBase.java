/* (C)2020 */
package ch.ethz.biol.cell.mpp.nrg.cachedcalculation;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMembershipWithFlags;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputPairMemo;
import org.anchoranalysis.anchor.mpp.overlap.MaxIntensityProjectionPair;
import org.anchoranalysis.anchor.mpp.pxlmark.VoxelizedMark;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.VoxelizedMarkMemo;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public abstract class CalculateOverlapMIPBase
        extends FeatureCalculation<Double, FeatureInputPairMemo> {

    private final int regionID;

    @Override
    protected Double execute(FeatureInputPairMemo params) {

        VoxelizedMarkMemo mark1 = params.getObj1();
        VoxelizedMarkMemo mark2 = params.getObj2();

        assert (mark1 != null);
        assert (mark2 != null);

        VoxelizedMark pm1 = mark1.voxelized();
        VoxelizedMark pm2 = mark2.voxelized();

        if (!pm1.getBoundingBoxMIP().intersection().existsWith(pm2.getBoundingBoxMIP())) {
            return 0.0;
        }

        MaxIntensityProjectionPair pair =
                new MaxIntensityProjectionPair(
                        pm1.getVoxelBoxMIP(),
                        pm2.getVoxelBoxMIP(),
                        regionMembershipForMark(mark1),
                        regionMembershipForMark(mark2));

        double overlap = pair.countIntersectingVoxels();

        return calculateOverlapResult(overlap, pair);
    }

    protected abstract Double calculateOverlapResult(
            double overlap, MaxIntensityProjectionPair pair);

    private RegionMembershipWithFlags regionMembershipForMark(VoxelizedMarkMemo mark) {
        return mark.getRegionMap().membershipWithFlagsForIndex(regionID);
    }
}
