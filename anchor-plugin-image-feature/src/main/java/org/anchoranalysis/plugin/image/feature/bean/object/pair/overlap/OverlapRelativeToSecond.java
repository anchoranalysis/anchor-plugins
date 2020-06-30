package org.anchoranalysis.plugin.image.feature.bean.object.pair.overlap;

import org.anchoranalysis.image.feature.object.input.FeatureInputPairObjects;

public class OverlapRelativeToSecond extends OverlapRelative {

	@Override
	protected int calcDenominator(FeatureInputPairObjects params) {
		return params.getSecond().numVoxelsOn();
	}
}
