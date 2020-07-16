/* (C)2020 */
package org.anchoranalysis.test.feature.plugins;

import static org.junit.Assert.assertEquals;

import java.util.Optional;
import org.anchoranalysis.core.name.store.SharedObjects;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.FeatureInitParams;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.session.FeatureSession;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.feature.shared.SharedFeatureMulti;
import org.anchoranalysis.test.LoggingFixture;

public class FeatureTestCalculator {

    private FeatureTestCalculator() {}

    public static <T extends FeatureInput> void assertDoubleResult(
            String message, Feature<T> feature, T params, double expectedResult)
            throws FeatureCalcException {
        assertDoubleResult(message, feature, params, Optional.empty(), expectedResult);
    }

    public static <T extends FeatureInput> void assertIntResult(
            String message, Feature<T> feature, T params, int expectedResult)
            throws FeatureCalcException {
        assertIntResult(message, feature, params, Optional.empty(), expectedResult);
    }

    public static <T extends FeatureInput> void assertDoubleResult(
            String message,
            Feature<T> feature,
            T params,
            Optional<SharedObjects> sharedObjects,
            double expectedResult)
            throws FeatureCalcException {
        assertResultTolerance(
                message, feature, params, createInitParams(sharedObjects), expectedResult, 1e-4);
    }

    public static <T extends FeatureInput> void assertIntResult(
            String message,
            Feature<T> feature,
            T params,
            Optional<SharedObjects> sharedObjects,
            int expectedResult)
            throws FeatureCalcException {
        assertResultTolerance(
                message, feature, params, createInitParams(sharedObjects), expectedResult, 1e-20);
    }

    private static FeatureInitParams createInitParams(Optional<SharedObjects> sharedObjects) {
        Optional<FeatureInitParams> mapped = sharedObjects.map(FeatureInitParams::new);
        return mapped.orElse(new FeatureInitParams());
    }

    private static <T extends FeatureInput> void assertResultTolerance(
            String message,
            Feature<T> feature,
            T params,
            FeatureInitParams initParams,
            double expectedResult,
            double delta)
            throws FeatureCalcException {
        double res = FeatureTestCalculator.calcSequentialSession(feature, params, initParams);
        assertEquals(message, expectedResult, res, delta);
    }

    private static <T extends FeatureInput> double calcSequentialSession(
            Feature<T> feature, T params, FeatureInitParams initParams)
            throws FeatureCalcException {

        FeatureCalculatorSingle<T> calculator =
                FeatureSession.with(
                        feature,
                        initParams,
                        new SharedFeatureMulti(),
                        LoggingFixture.suppressedLogErrorReporter());

        return calculator.calc(params);
    }
}
