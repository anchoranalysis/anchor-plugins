/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.obj.intersecting;

import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.plugin.image.feature.bean.object.single.shared.intersecting.NumberIntersectingObjects;
import org.junit.Test;

public class NumberIntersectingObjectsTest {

    static final int EXPECTED_FIRST = 1;
    static final int EXPECTED_SECOND = 2;
    static final int EXPECTED_SECOND_LAST = 1;
    static final int EXPECTED_NO_INTERSECTION = 0;

    @Test
    public void testSameSizes()
            throws FeatureCalcException, InitException, OperationFailedException {
        testForSpecificExpectedValues("sameSize", true);
    }

    @Test
    public void testDifferentSizes()
            throws FeatureCalcException, InitException, OperationFailedException {
        testForSpecificExpectedValues("differentSize", false);
    }

    // As expected-values are the same, we have a helper function
    private void testForSpecificExpectedValues(String messagePrefix, boolean sameSize)
            throws OperationFailedException, FeatureCalcException, InitException {
        InteresectingObjectsTestHelper.testPositions(
                messagePrefix,
                new NumberIntersectingObjects(),
                sameSize,
                EXPECTED_FIRST,
                EXPECTED_SECOND,
                EXPECTED_NO_INTERSECTION,
                EXPECTED_NO_INTERSECTION);
    }
}
