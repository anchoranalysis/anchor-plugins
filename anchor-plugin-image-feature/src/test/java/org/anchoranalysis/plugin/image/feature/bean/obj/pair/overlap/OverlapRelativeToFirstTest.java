/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.obj.pair.overlap;

import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.plugin.image.feature.bean.obj.pair.ParamsFixtureHelper;
import org.anchoranalysis.plugin.image.feature.bean.object.pair.overlap.OverlapRelativeToFirst;
import org.junit.Test;

public class OverlapRelativeToFirstTest {

    @Test
    public void testOverlapping() throws FeatureCalcException, InitException {
        ParamsFixtureHelper.testTwoSizesOverlappingDouble(
                new OverlapRelativeToFirst(),
                0.8713222261609358,
                ParamsFixtureHelper.OVERLAP_RATIO_TO_OTHER_SAME_SIZE);
    }
}
