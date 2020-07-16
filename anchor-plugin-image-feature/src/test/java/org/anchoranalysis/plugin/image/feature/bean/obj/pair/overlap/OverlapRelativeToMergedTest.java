/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.obj.pair.overlap;

import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.plugin.image.feature.bean.obj.pair.ParamsFixtureHelper;
import org.anchoranalysis.plugin.image.feature.bean.object.pair.overlap.OverlapRelativeToMerged;
import org.junit.Test;

public class OverlapRelativeToMergedTest {

    @Test
    public void testOverlapping() throws FeatureCalcException, InitException {
        ParamsFixtureHelper.testTwoSizesOverlappingDouble(
                new OverlapRelativeToMerged(), 0.651643690349947, 0.6530911221799004);
    }
}
