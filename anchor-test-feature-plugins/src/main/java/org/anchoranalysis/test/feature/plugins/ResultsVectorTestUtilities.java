/*-
 * #%L
 * anchor-test-feature-plugins
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

package org.anchoranalysis.test.feature.plugins;

import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.feature.results.ResultsVector;

/**
 * Utility class for testing ResultsVector objects.
 *
 * <p>This class provides methods to assert the equality of ResultsVector objects
 * with expected values, using a small epsilon for floating-point comparisons.</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResultsVectorTestUtilities {

    private static double eps = 1e-12;

    /**
     * Asserts that a ResultsVector matches the expected values within a small epsilon.
     *
     * <p>If the assertion fails, it prints debug information to the console before throwing an AssertionError.</p>
     *
     * @param results the ResultsVector to check
     * @param expectedVals the expected values to compare against
     * @throws AssertionError if the ResultsVector does not match the expected values within the epsilon
     */
    public static void assertCalc(ResultsVector results, Object... expectedVals) {
        boolean areEquals = results.equalsPrecision(eps, expectedVals);
        if (!areEquals) {
            System.out.println("assertCalc failed"); // NOSONAR
            System.out.printf("Results: \t%s%n", results); // NOSONAR
        }
        assertTrue(areEquals);
    }
}