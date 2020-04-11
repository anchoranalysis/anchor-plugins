package ch.ethz.biol.cell.mpp.nrg.feature.pair;

import org.anchoranalysis.anchor.mpp.feature.nrg.elem.NRGElemPairCalcParams;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.feature.cache.CacheableParams;
import org.anchoranalysis.feature.calc.FeatureCalcException;

import ch.ethz.biol.cell.mpp.nrg.cachedcalculation.OverlapCalculationMaskGlobalMiddleQuantiles;

public abstract class OverlapMaskQuantiles extends OverlapMaskSingleRegion {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private double quantileLow = 0;
	
	@BeanField
	private double quantileHigh = 1;
	// END BEAN PROPERTIES

	protected double overlapWithQuantiles( CacheableParams<NRGElemPairCalcParams> params ) throws FeatureCalcException {
		
		try {
			return params.calc(
				new OverlapCalculationMaskGlobalMiddleQuantiles(
					getRegionID(),
					getNrgIndex(),
					(byte) getMaskValue(),
					quantileLow,
					quantileHigh
				)
			);
		} catch (ExecuteException e) {
			throw new FeatureCalcException(e);
		}							
	}

	public double getQuantileLow() {
		return quantileLow;
	}

	public void setQuantileLow(double quantileLow) {
		this.quantileLow = quantileLow;
	}

	public double getQuantileHigh() {
		return quantileHigh;
	}

	public void setQuantileHigh(double quantileHigh) {
		this.quantileHigh = quantileHigh;
	}
}
