/* (C)2020 */
package org.anchoranalysis.plugin.operator.feature.bean.replace;

import org.anchoranalysis.feature.input.FeatureInput;

public class ReplaceInfinity<T extends FeatureInput> extends ReplaceUnusualValue<T> {

    @Override
    protected boolean isResultUnusual(double featureResult) {
        return Double.isInfinite(featureResult);
    }
}
