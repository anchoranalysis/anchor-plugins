package org.anchoranalysis.image.feature.bean.list;

import org.anchoranalysis.plugin.operator.feature.bean.score.ZScore;

/**
 * Similar to FeatureListProviderPermute but embeds the feature in a GaussianScore
 * 
 * @author Owen Feehan
 *
 */
public class FeatureListProviderPermuteZScore extends FeatureListProviderPermuteFirstSecondOrder {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FeatureListProviderPermuteZScore() {
		super( ()-> new ZScore<>(),
			-1 * Double.MAX_VALUE,
			Double.MAX_VALUE );
	}
}
