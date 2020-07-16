/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme;

import org.anchoranalysis.anchor.mpp.feature.addcriteria.BBoxIntersection;
import org.anchoranalysis.anchor.mpp.feature.nrg.scheme.NRGScheme;
import org.anchoranalysis.anchor.mpp.feature.nrg.scheme.NRGSchemeWithSharedFeatures;
import org.anchoranalysis.anchor.mpp.regionmap.RegionMapSingleton;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.bean.list.FeatureListFactory;
import org.anchoranalysis.feature.shared.SharedFeatureMulti;
import org.anchoranalysis.plugin.mpp.feature.bean.memo.ind.Size;
import org.anchoranalysis.plugin.mpp.feature.bean.memo.pair.overlap.OverlapNumVoxels;
import org.anchoranalysis.plugin.operator.feature.bean.arithmetic.MultiplyByConstant;
import org.anchoranalysis.test.LoggingFixture;

public class NRGSchemeFixture {

    private NRGSchemeFixture() {}

    /**
     * An NRG scheme that is rewarded by larger marks but with a penalty for overlap.
     *
     * <p><div> Specifically it is: <code>
     *  sum(voxels across each mark) - weight * sum(overlapping voxels regions between marks)
     * </code> </div>
     *
     * @param weightOverlap a positive integer indicating how much to penalize the overlapping
     *     voxels by, the higher the greater the penalty.
     * @return
     * @throws CreateException
     */
    public static NRGSchemeWithSharedFeatures sizeMinusWeightedOverlap(double weightOverlap)
            throws CreateException {
        return new NRGSchemeWithSharedFeatures(
                createNRGScheme(weightOverlap),
                new SharedFeatureMulti(),
                LoggingFixture.suppressedLogErrorReporter());
    }

    private static NRGScheme createNRGScheme(double weightOverlap) throws CreateException {
        return new NRGScheme(
                FeatureListFactory.from(new Size()),
                FeatureListFactory.from(
                        new MultiplyByConstant<>(new OverlapNumVoxels(), -1 * weightOverlap)),
                FeatureListFactory.empty(),
                RegionMapSingleton.instance(),
                new BBoxIntersection());
    }
}
