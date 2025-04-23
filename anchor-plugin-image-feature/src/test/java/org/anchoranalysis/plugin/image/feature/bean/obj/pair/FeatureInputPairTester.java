/*-
 * #%L
 * anchor-plugin-image-feature
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

package org.anchoranalysis.plugin.image.feature.bean.obj.pair;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.image.feature.input.FeatureInputPairObjects;
import org.anchoranalysis.test.feature.plugins.FeatureTestCalculator;
import org.anchoranalysis.test.feature.plugins.FeatureTestCalculatorDuo;
import org.anchoranalysis.test.feature.plugins.objects.FeatureInputOverlappingCircleFixture;

/** Utility class for testing features that operate on {@link FeatureInputPairObjects}. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FeatureInputPairTester {

    /** A particular result that should be the same for the same-size case in both directions. */
    public static final double OVERLAP_RATIO_TO_OTHER_SAME_SIZE = 0.7901453385324353;

    /** The overlap ratio expected to max-volume. */
    public static final double OVERLAP_RATIO_TO_MAX_VOLUME_DIFFERENT_SIZE = 0.7210325608682898;

    /**
     * Tests two overlapping circles, assuming feature returns 0 if non-overlapping, and double
     * otherwise.
     *
     * <p>Two conditions are tested - when the circles are the same-size, and when not.
     *
     * @param feature the {@link Feature} to test
     * @param expectedDifferentSize expected result for different-sized circles
     * @param expectedSameSize expected result for same-sized circles
     * @throws FeatureCalculationException if the feature calculation fails
     */
    public static void testTwoSizesOverlappingDouble(
            Feature<FeatureInputPairObjects> feature,
            double expectedDifferentSize,
            double expectedSameSize)
            throws FeatureCalculationException {

        testOverlappingCirclesDoubleSize(feature, expectedSameSize, true);
        testOverlappingCirclesDoubleSize(feature, expectedDifferentSize, false);
    }

    /**
     * Tests two overlapping circles, assuming feature returns 0 if non-overlapping, and integer
     * otherwise.
     *
     * <p>Two conditions are tested - when the circles are the same-size, and when not.
     *
     * @param feature the {@link Feature} to test
     * @param expectedSameSize expected result for same-sized circles
     * @param expectedDifferentSize expected result for different-sized circles
     * @throws FeatureCalculationException if the feature calculation fails
     */
    public static void testTwoSizesOverlappingInt(
            Feature<FeatureInputPairObjects> feature,
            int expectedSameSize,
            int expectedDifferentSize)
            throws FeatureCalculationException {

        testOverlappingCirclesIntSize(feature, expectedSameSize, true);
        testOverlappingCirclesIntSize(feature, expectedDifferentSize, false);
    }

    /**
     * Simple test on a feature for an expected value (of type int) for two-overlapping-circles of
     * different sizes.
     *
     * @param feature the {@link Feature} to test
     * @param expected the expected result
     * @throws FeatureCalculationException if the feature calculation fails
     * @throws InitializeException if the feature initialization fails
     */
    public static void testSimpleInt(Feature<FeatureInputPairObjects> feature, int expected)
            throws FeatureCalculationException {
        FeatureTestCalculator.assertIntResult(
                "simple",
                feature,
                FeatureInputOverlappingCircleFixture.twoOverlappingCircles(false),
                expected);
    }

    /**
     * Simple test on a feature for an expected value (of type double) for two-overlapping-circles
     * of different sizes.
     *
     * @param feature the {@link Feature} to test
     * @param expected the expected result
     * @throws FeatureCalculationException if the feature calculation fails
     * @throws InitializeException if the feature initialization fails
     */
    public static void testSimpleDouble(Feature<FeatureInputPairObjects> feature, double expected)
            throws FeatureCalculationException {
        FeatureTestCalculator.assertDoubleResult(
                "simple",
                feature,
                FeatureInputOverlappingCircleFixture.twoOverlappingCircles(false),
                expected);
    }

    /**
     * Tests overlapping circles with integer results.
     *
     * @param feature the {@link Feature} to test
     * @param expected the expected result
     * @param sameSize whether the circles are the same size
     * @throws FeatureCalculationException if the feature calculation fails
     * @throws InitializeException if the feature initialization fails
     */
    public static void testOverlappingCirclesIntSize(
            Feature<FeatureInputPairObjects> feature, int expected, boolean sameSize)
            throws FeatureCalculationException {
        FeatureTestCalculatorDuo.assertIntResult(
                message(sameSize),
                feature,
                FeatureInputOverlappingCircleFixture.twoOverlappingCircles(sameSize),
                FeatureInputOverlappingCircleFixture.twoNonOverlappingCircles(sameSize),
                expected,
                0);
    }

    /**
     * Tests overlapping circles with double results.
     *
     * @param feature the {@link Feature} to test
     * @param expected the expected result
     * @param sameSize whether the circles are the same size
     * @throws FeatureCalculationException if the feature calculation fails
     * @throws InitializeException if the feature initialization fails
     */
    private static void testOverlappingCirclesDoubleSize(
            Feature<FeatureInputPairObjects> feature, double expected, boolean sameSize)
            throws FeatureCalculationException {
        FeatureTestCalculatorDuo.assertDoubleResult(
                message(sameSize),
                feature,
                FeatureInputOverlappingCircleFixture.twoOverlappingCircles(sameSize),
                FeatureInputOverlappingCircleFixture.twoNonOverlappingCircles(sameSize),
                expected,
                0);
    }

    /**
     * Creates a message describing the test case.
     *
     * @param sameSize whether the circles are the same size
     * @return a descriptive message
     */
    private static String message(boolean sameSize) {
        return sameSize ? "same-sized overlapping circles" : "different-sized overlapping circles";
    }
}
