/* (C)2020 */
package org.anchoranalysis.test.feature.plugins;

import static org.junit.Assert.assertTrue;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.feature.calc.results.ResultsVector;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResultsVectorTestUtilities {

    private static double eps = 1e-16;

    public static void assertCalc(ResultsVector rv, Object... expectedVals) {
        boolean areEquals = rv.equalsPrecision(eps, expectedVals);
        if (!areEquals) {
            System.out.println("assertCalc failed"); // NOSONAR
            System.out.printf("Results: \t%s%n", rv); // NOSONAR
        }
        assertTrue(areEquals);
    }
}
