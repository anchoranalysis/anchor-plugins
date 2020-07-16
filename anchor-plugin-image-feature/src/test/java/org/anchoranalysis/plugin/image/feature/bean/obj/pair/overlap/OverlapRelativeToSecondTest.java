/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.obj.pair.overlap;

import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.plugin.image.feature.bean.obj.pair.ParamsFixtureHelper;
import org.anchoranalysis.plugin.image.feature.bean.object.pair.overlap.OverlapRelativeToSecond;
import org.junit.Test;

public class OverlapRelativeToSecondTest {

    @Test
    public void testOverlapping() throws FeatureCalcException, InitException {
        ParamsFixtureHelper.testTwoSizesOverlappingDouble(
                new OverlapRelativeToSecond(),
                ParamsFixtureHelper.OVERLAP_RATIO_TO_MAX_VOLUME_DIFFERENT_SIZE,
                ParamsFixtureHelper.OVERLAP_RATIO_TO_OTHER_SAME_SIZE);
    }
}
