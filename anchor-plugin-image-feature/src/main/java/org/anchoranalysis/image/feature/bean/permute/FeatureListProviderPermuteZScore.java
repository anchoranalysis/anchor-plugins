package org.anchoranalysis.image.feature.bean.permute;

import org.anchoranalysis.plugin.operator.feature.bean.score.ZScore;

/**
 * Similar to FeatureListProviderPermute but embeds the feature in a GaussianScore
 * 
 * @author Owen Feehan
 *
 */
public class FeatureListProviderPermuteZScore extends FeatureListProviderPermuteFirstSecondOrder {

	public FeatureListProviderPermuteZScore() {
		super(
			ZScore::new,
			-1 * Double.MAX_VALUE,
			Double.MAX_VALUE
		);
	}
}
