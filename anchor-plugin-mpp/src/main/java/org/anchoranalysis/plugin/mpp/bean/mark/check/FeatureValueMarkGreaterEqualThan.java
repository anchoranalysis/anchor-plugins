/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.mark.check;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMap;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureInputMark;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;

public class FeatureValueMarkGreaterEqualThan extends FeatureValueCheckMark<FeatureInputMark> {

    @Override
    protected FeatureInputMark createFeatureCalcParams(
            Mark mark, RegionMap regionMap, NRGStackWithParams nrgStack) {
        return new FeatureInputMark(mark, Optional.of(nrgStack.getDimensions()));
    }
}
