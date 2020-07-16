/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.list.permute;

import org.anchoranalysis.feature.input.FeatureInputParams;
import org.anchoranalysis.plugin.operator.feature.bean.score.GaussianScore;

/**
 * Similar to FeatureListProviderPermute but embeds the feature in a GaussianScore
 *
 * @author Owen Feehan
 */
public class PermuteGaussianScore<T extends FeatureInputParams> extends PermuteFirstSecondOrder<T> {

    public PermuteGaussianScore() {
        super(GaussianScore::new, 0, 1);
    }
}
