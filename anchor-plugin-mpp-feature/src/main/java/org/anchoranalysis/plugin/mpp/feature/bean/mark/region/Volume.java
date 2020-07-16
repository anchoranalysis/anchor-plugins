/* (C)2020 */
package org.anchoranalysis.plugin.mpp.feature.bean.mark.region;

import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureInputMark;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;

public class Volume extends FeatureMarkRegion {

    @Override
    public double calc(SessionInput<FeatureInputMark> input) throws FeatureCalcException {
        return input.get().getMark().volume(getRegionID());
    }
}
