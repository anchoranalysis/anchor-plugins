package org.anchoranalysis.test.feature.plugins;

import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.params.FeatureCalcParams;

/** Tests that consider two possibilities: positive and negative */
public class FeatureTestCalculatorDuo {

	public static void assertDoubleResult(
		String message,
		Feature feature,
		FeatureCalcParams paramsPositive,
		FeatureCalcParams paramsNegative,
		double expectedResultPositive,
		double expectedResultNegative
	) throws FeatureCalcException, InitException {
		FeatureTestCalculator.assertDoubleResult(
			positiveMessage(message),
			feature,
			paramsPositive,
			expectedResultPositive
		);
		FeatureTestCalculator.assertDoubleResult(
			negativeMessage(message),
			feature,
			paramsNegative,
			expectedResultNegative
		);
	}
	
	public static void assertIntResult(
		String message,
		Feature feature,
		FeatureCalcParams paramsPositive,
		FeatureCalcParams paramsNegative,
		int expectedResultPositive,
		int expectedResultNegative
	) throws FeatureCalcException, InitException {
		FeatureTestCalculator.assertIntResult(
			positiveMessage(message),
			feature,
			paramsPositive,
			expectedResultPositive
		);
		FeatureTestCalculator.assertIntResult(
			negativeMessage(message),
			feature,
			paramsNegative,
			expectedResultNegative
		);
	}
	
	private static String positiveMessage(String message) {
		return message + " (positive case)";
	}
	
	private static String negativeMessage(String message) {
		return message + " (negative case)";
	}
}
