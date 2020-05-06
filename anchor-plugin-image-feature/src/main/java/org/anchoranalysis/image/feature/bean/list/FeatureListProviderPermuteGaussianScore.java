package org.anchoranalysis.image.feature.bean.list;



import org.anchoranalysis.plugin.operator.feature.bean.score.GaussianScore;

/**
 * Similar to FeatureListProviderPermute but embeds the feature in a GaussianScore
 * 
 * @author Owen Feehan
 *
 */
public class FeatureListProviderPermuteGaussianScore extends FeatureListProviderPermuteFirstSecondOrder {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FeatureListProviderPermuteGaussianScore() {
		super( ()-> new GaussianScore<>(), 0, 1 );
	}



}
