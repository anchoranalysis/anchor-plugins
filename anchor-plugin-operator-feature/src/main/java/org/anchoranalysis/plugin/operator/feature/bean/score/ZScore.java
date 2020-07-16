/* (C)2020 */
package org.anchoranalysis.plugin.operator.feature.bean.score;

import org.anchoranalysis.core.functional.Operation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.math.statistics.FirstSecondOrderStatistic;

// Z-score of a value
public class ZScore<T extends FeatureInput> extends FeatureStatScore<T> {

    @Override
    protected double deriveScore(
            double featureValue, double mean, Operation<Double, FeatureCalcException> stdDev)
            throws FeatureCalcException {

        double zScore =
                FirstSecondOrderStatistic.calcZScore(featureValue, mean, stdDev.doOperation());
        assert (!Double.isNaN(zScore));
        return zScore;
    }
}
