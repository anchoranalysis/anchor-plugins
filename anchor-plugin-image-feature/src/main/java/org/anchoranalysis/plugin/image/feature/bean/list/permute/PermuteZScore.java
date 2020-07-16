/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.list.permute;

import org.anchoranalysis.feature.input.FeatureInputParams;
import org.anchoranalysis.plugin.operator.feature.bean.score.ZScore;

/**
 * Similar to FeatureListProviderPermute but embeds the feature in a GaussianScore
 *
 * @author Owen Feehan
 */
public class PermuteZScore<T extends FeatureInputParams> extends PermuteFirstSecondOrder<T> {

    public PermuteZScore() {
        super(ZScore::new, -1 * Double.MAX_VALUE, Double.MAX_VALUE);
    }
}
