package org.anchoranalysis.plugin.mpp.feature.bean.memo.pair.overlap;

import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputPairMemo;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.plugin.mpp.feature.bean.memo.pair.FeaturePairMemoSingleRegion;

import ch.ethz.biol.cell.mpp.nrg.cachedcalculation.OverlapMIPRatioCalculation;

public abstract class OverlapMIPBase extends FeaturePairMemoSingleRegion {

	// START BEAN PROPERTIES
	@BeanField
	private boolean mip = false;
	// END BEAN PROPERTIES
		
	@Override
	protected double overlappingNumVoxels(SessionInput<FeatureInputPairMemo> input) throws FeatureCalcException {
		if (mip) {
			return input.calc(
				new OverlapMIPRatioCalculation( getRegionID() )
			);
		} else {
			return super.overlappingNumVoxels(input);
		}
	}
	
	public boolean isMip() {
		return mip;
	}

	public void setMip(boolean mip) {
		this.mip = mip;
	}
}
