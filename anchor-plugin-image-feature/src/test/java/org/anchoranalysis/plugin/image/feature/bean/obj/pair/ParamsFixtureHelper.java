package org.anchoranalysis.plugin.image.feature.bean.obj.pair;

import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.test.feature.plugins.FeatureTestCalculator;
import org.anchoranalysis.test.feature.plugins.FeatureTestCalculatorDuo;
import org.anchoranalysis.test.feature.plugins.ParamsFixture;

public class ParamsFixtureHelper {
	
	/** A particular result that should be the same for the same-size case in both directions */
	public static final double OVERLAP_RATIO_TO_OTHER_SAME_SIZE = 0.7901453385324353;
	
	/** The overlap ratio expected to max-volume */
	public static final double OVERLAP_RATIO_TO_MAX_VOLUME_DIFFERENT_SIZE = 0.7210325608682898;
	
	private ParamsFixtureHelper() {
	}
	
	/** 
	 * Tests two overlapping circles, assuming feature returns 0 if non-overlapping, and double otherwise
	 * 
	 * <p>Two conditions are tested - when the circles are the same-size, and when not</p>
	 **/
	public static void testTwoSizesOverlappingDouble(
		Feature feature,
		double expectedDifferentSize,
		double expectedSameSize
	) throws FeatureCalcException, InitException {
		
		testOverlappingCirclesDoubleSize(feature, expectedSameSize, true);
		testOverlappingCirclesDoubleSize(feature, expectedDifferentSize, false);
	}
	
	/** 
	 * Tests two overlapping circles, assuming feature returns 0 if non-overlapping, and integer otherwise
	 * 
	 * <p>Two conditions are tested - when the circles are the same-size, and when not</p>
	 */
	public static void testTwoSizesOverlappingInt(
		Feature feature,
		int expectedSameSize,
		int expectedDifferentSize
	) throws FeatureCalcException, InitException {
		
		testOverlappingCirclesIntSize(feature, expectedSameSize, true);
		testOverlappingCirclesIntSize(feature, expectedDifferentSize, false);
	}
	
	/**
	 * Simple test on a feature for an expected value for two-overlapping-circles of different sizes
	 * @param feature
	 * @param expected
	 * @throws FeatureCalcException
	 * @throws InitException
	 */
	public static void testSimple(
		Feature feature,
		int expected
	) throws FeatureCalcException, InitException {
		FeatureTestCalculator.assertIntResult(
			"simple",
			feature,
			ParamsFixture.twoOverlappingCircles(false),
			expected
		);
	}
	
	private static void testOverlappingCirclesDoubleSize( Feature feature, double expected, boolean sameSize ) throws FeatureCalcException, InitException {
		FeatureTestCalculatorDuo.assertDoubleResult(
			message(sameSize),
			feature,
			ParamsFixture.twoOverlappingCircles(sameSize),
			ParamsFixture.twoNonOverlappingCircles(sameSize),
			expected,
			0
		);
	}
	
	public static void testOverlappingCirclesIntSize( Feature feature, int expected, boolean sameSize ) throws FeatureCalcException, InitException {
		FeatureTestCalculatorDuo.assertIntResult(
			message(sameSize),
			feature,
			ParamsFixture.twoOverlappingCircles(sameSize),
			ParamsFixture.twoNonOverlappingCircles(sameSize),
			expected,
			0
		);
	}
	
	private static String message(boolean sameSize) {
		return sameSize ? "same-sized overlapping circles" : "different-sized overlapping circles";
	}
}
