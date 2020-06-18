package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme;

import java.util.Arrays;

import org.anchoranalysis.anchor.mpp.feature.addcriteria.BBoxIntersection;
import org.anchoranalysis.anchor.mpp.feature.bean.nrgscheme.NRGScheme;
import org.anchoranalysis.anchor.mpp.feature.nrg.scheme.NRGSchemeWithSharedFeatures;
import org.anchoranalysis.feature.shared.SharedFeatureMulti;
import org.anchoranalysis.plugin.mpp.feature.bean.memo.ind.Size;
import org.anchoranalysis.plugin.mpp.feature.bean.memo.pair.overlap.OverlapNumVoxels;
import org.anchoranalysis.plugin.operator.feature.bean.arithmetic.MultiplyByConstant;
import org.anchoranalysis.test.LoggingFixture;
import org.anchoranalysis.test.bean.BeanTestChecker;

public class NRGSchemeFixture {
	
	private NRGSchemeFixture() {}
	
	/**
	 * An NRG scheme that is rewarded by larger marks but with a penalty for overlap.
	 * 
	 * <div>
	 * Specifically it is:
	 * <code>
	 *  sum(voxels across each mark) - weight * sum(overlapping voxels regions between marks)
	 * </code>
	 * </div>
	 * 
	 * @param weightOverlap a positive integer indicating how much to penalize the overlapping voxels by, the higher the greater the penalty.
	 * @return
	 */
	public static NRGSchemeWithSharedFeatures sizeMinusWeightedOverlap( double weightOverlap ) {
		
		NRGScheme scheme = new NRGScheme();
		scheme.setElemInd(
			Arrays.asList( new Size() )
		);
		scheme.setElemPair(
			Arrays.asList( new MultiplyByConstant<>(new OverlapNumVoxels(),-1 * weightOverlap) )
		);
		
		scheme.setPairAddCriteria( new BBoxIntersection() );
		BeanTestChecker.check(scheme);
		
		return new NRGSchemeWithSharedFeatures(
			scheme,
			new SharedFeatureMulti(),
			10,
			LoggingFixture.suppressedLogErrorReporter()
		);		
	}
}
