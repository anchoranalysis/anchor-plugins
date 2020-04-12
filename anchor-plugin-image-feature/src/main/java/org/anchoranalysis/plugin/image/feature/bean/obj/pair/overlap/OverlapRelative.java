package org.anchoranalysis.plugin.image.feature.bean.obj.pair.overlap;

import org.anchoranalysis.feature.cache.CacheableParams;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.objmask.pair.FeatureObjMaskPair;
import org.anchoranalysis.image.feature.objmask.pair.FeatureObjMaskPairParams;
import org.anchoranalysis.plugin.image.feature.obj.pair.overlap.OverlapRatioUtilities;


/**
 * Expresses the number of intersecting pixels between two objects as a ratio to something else (denominator)
 * 
 * @author owen
 *
 */
public abstract class OverlapRelative extends FeatureObjMaskPair {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public double calc(CacheableParams<FeatureObjMaskPairParams> paramsCacheable)
			throws FeatureCalcException {
		
		FeatureObjMaskPairParams params = paramsCacheable.getParams();

		return OverlapRatioUtilities.overlapRatioTo(
			params,
			() -> calcDenominator(params)
		);
	}
	
	protected abstract int calcDenominator( FeatureObjMaskPairParams params );
}
