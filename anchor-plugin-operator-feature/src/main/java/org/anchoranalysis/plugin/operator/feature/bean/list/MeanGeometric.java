/* (C)2020 */
package org.anchoranalysis.plugin.operator.feature.bean.list;

import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.operator.FeatureListElem;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;
import org.apache.commons.math3.stat.descriptive.moment.GeometricMean;

// Geometric mean
public class MeanGeometric<T extends FeatureInput> extends FeatureListElem<T> {

    private GeometricMean meanCalculator = new GeometricMean();

    @Override
    public double calc(SessionInput<T> input) throws FeatureCalcException {

        double[] result = new double[getList().size()];

        for (int i = 0; i < getList().size(); i++) {
            Feature<T> elem = getList().get(i);
            result[i] = input.calc(elem);
        }

        return meanCalculator.evaluate(result);
    }

    @Override
    public String getDscrLong() {
        StringBuilder sb = new StringBuilder();
        sb.append("geo_mean(");
        sb.append(descriptionForList(","));
        sb.append(")");
        return sb.toString();
    }
}
