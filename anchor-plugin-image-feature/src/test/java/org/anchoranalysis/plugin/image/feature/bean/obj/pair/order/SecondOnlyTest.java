/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.obj.pair.order;

import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.object.pair.Second;
import org.anchoranalysis.plugin.image.feature.bean.obj.pair.ParamsFixtureHelper;
import org.junit.Test;

public class SecondOnlyTest {

    @Test
    public void testOverlapping() throws FeatureCalcException, InitException {

        ParamsFixtureHelper.testSimpleInt(OrderHelper.addFeatureToOrder(new Second()), 3409);
    }
}
