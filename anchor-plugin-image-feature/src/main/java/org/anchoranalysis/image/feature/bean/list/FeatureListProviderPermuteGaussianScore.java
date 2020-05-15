package org.anchoranalysis.image.feature.bean.list;



import org.anchoranalysis.plugin.operator.feature.bean.score.GaussianScore;

/**
 * Similar to FeatureListProviderPermute but embeds the feature in a GaussianScore
 * 
 * @author Owen Feehan
 *
 */
public class FeatureListProviderPermuteGaussianScore extends FeatureListProviderPermuteFirstSecondOrder {

	public FeatureListProviderPermuteGaussianScore() {
		super( ()-> new GaussianScore<>(), 0, 1 );
	}



}
