/* (C)2020 */
package org.anchoranalysis.test.feature.plugins;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;

/** Tests that consider two possibilities: positive and negative */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FeatureTestCalculatorDuo {

    public static <T extends FeatureInput> void assertDoubleResult(
            String message,
            Feature<T> feature,
            T paramsPositive,
            T paramsNegative,
            double expectedResultPositive,
            double expectedResultNegative)
            throws FeatureCalcException {
        FeatureTestCalculator.assertDoubleResult(
                positiveMessage(message), feature, paramsPositive, expectedResultPositive);
        FeatureTestCalculator.assertDoubleResult(
                negativeMessage(message), feature, paramsNegative, expectedResultNegative);
    }

    public static <T extends FeatureInput> void assertIntResult(
            String message,
            Feature<T> feature,
            T paramsPositive,
            T paramsNegative,
            int expectedResultPositive,
            int expectedResultNegative)
            throws FeatureCalcException {
        FeatureTestCalculator.assertIntResult(
                positiveMessage(message), feature, paramsPositive, expectedResultPositive);
        FeatureTestCalculator.assertIntResult(
                negativeMessage(message), feature, paramsNegative, expectedResultNegative);
    }

    private static String positiveMessage(String message) {
        return message + " (positive case)";
    }

    private static String negativeMessage(String message) {
        return message + " (negative case)";
    }
}
