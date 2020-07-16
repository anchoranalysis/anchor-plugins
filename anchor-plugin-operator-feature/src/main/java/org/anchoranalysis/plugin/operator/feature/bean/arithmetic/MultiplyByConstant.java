/* (C)2020 */
package org.anchoranalysis.plugin.operator.feature.bean.arithmetic;

import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.plugin.operator.feature.bean.FeatureGenericWithValue;

public class MultiplyByConstant<T extends FeatureInput> extends FeatureGenericWithValue<T> {

    public MultiplyByConstant() {
        // Standard Bean Constructor
    }

    public MultiplyByConstant(Feature<T> feature, double value) {
        setItem(feature);
        setValue(value);
    }

    @Override
    protected double combineValueAndFeature(double value, double featureResult) {
        return value * featureResult;
    }

    @Override
    protected String combineDscr(String valueDscr, String featureDscr) {
        return valueDscr + " * " + featureDscr;
    }
}
