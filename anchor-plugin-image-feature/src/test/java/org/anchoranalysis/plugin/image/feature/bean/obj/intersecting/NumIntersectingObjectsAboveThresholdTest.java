/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.obj.intersecting;

import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.plugin.image.feature.bean.object.single.shared.intersecting.FeatureIntersectingObjects;
import org.anchoranalysis.plugin.image.feature.bean.object.single.shared.intersecting.NumberIntersectingObjectsAboveThreshold;
import org.junit.Test;

public class NumIntersectingObjectsAboveThresholdTest {

    private static final int EXPECTED_REJECTED_UNDER_THRESHOLD = 0;

    private static final FeatureIntersectingObjects FEATURE =
            FeatureHelper.createWithThreshold(new NumberIntersectingObjectsAboveThreshold());

    @Test
    public void testSameSizes()
            throws FeatureCalcException, InitException, OperationFailedException {
        InteresectingObjectsTestHelper.testPositions(
                "sameSize",
                FEATURE,
                true,
                EXPECTED_REJECTED_UNDER_THRESHOLD,
                EXPECTED_REJECTED_UNDER_THRESHOLD,
                NumberIntersectingObjectsTest.EXPECTED_NO_INTERSECTION,
                NumberIntersectingObjectsTest.EXPECTED_NO_INTERSECTION);
    }

    @Test
    public void testDifferentSizes()
            throws FeatureCalcException, InitException, OperationFailedException {
        InteresectingObjectsTestHelper.testPositions(
                "differentSize",
                FEATURE,
                false,
                EXPECTED_REJECTED_UNDER_THRESHOLD,
                NumberIntersectingObjectsTest.EXPECTED_SECOND,
                NumberIntersectingObjectsTest.EXPECTED_NO_INTERSECTION,
                NumberIntersectingObjectsTest.EXPECTED_NO_INTERSECTION);
    }
}
