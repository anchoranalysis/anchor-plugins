/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.obj.intersecting;

import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.plugin.image.feature.bean.object.single.shared.intersecting.FeatureIntersectingObjects;
import org.anchoranalysis.plugin.image.feature.bean.object.single.shared.intersecting.MinFeatureIntersectingObjectsAboveThreshold;
import org.junit.Test;

public class MinFeatureIntersectingObjectsAboveThresholdTest {

    private static final FeatureIntersectingObjects FEATURE =
            FeatureHelper.createWithThreshold(new MinFeatureIntersectingObjectsAboveThreshold());

    /**
     * We expect always the same number of pixels apart from the last object which has 0 neighbors
     *
     * @throws FeatureCalcException
     * @throws InitException
     * @throws OperationFailedException
     */
    @Test
    public void testSameSizes()
            throws FeatureCalcException, InitException, OperationFailedException {
        InteresectingObjectsTestHelper.testPositions(
                "sameSize",
                FEATURE,
                true,
                FeatureHelper.VALUE_NO_OBJECTS,
                FeatureHelper.VALUE_NO_OBJECTS,
                FeatureHelper.VALUE_NO_OBJECTS,
                FeatureHelper.VALUE_NO_OBJECTS);
    }

    /**
     * We expect a growing number of pixels apart from the last object which has 0 neighbors
     *
     * @throws FeatureCalcException
     * @throws InitException
     * @throws OperationFailedException
     */
    @Test
    public void testDifferentSizes()
            throws FeatureCalcException, InitException, OperationFailedException {
        InteresectingObjectsTestHelper.testPositions(
                "differentSize",
                FEATURE,
                false,
                FeatureHelper.VALUE_NO_OBJECTS,
                FeatureHelper.EXPECTED_NUM_PIXELS_SECOND_CIRCLE,
                FeatureHelper.VALUE_NO_OBJECTS,
                FeatureHelper.VALUE_NO_OBJECTS);
    }
}
