/* (C)2020 */
package org.anchoranalysis.plugin.operator.feature.bean.arithmetic;

import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.plugin.operator.feature.bean.FeatureGenericWithValue;

public class ConstantToThePowerOf<T extends FeatureInput> extends FeatureGenericWithValue<T> {

    @Override
    protected double combineValueAndFeature(double value, double featureResult) {
        return Math.pow(value, featureResult);
    }

    @Override
    protected String combineDscr(String valueDscr, String featureDscr) {
        return valueDscr + "^" + featureDscr;
    }
}
