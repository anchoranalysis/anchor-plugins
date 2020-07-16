/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.pair.overlap;

import org.anchoranalysis.image.feature.object.input.FeatureInputPairObjects;

/**
 * Overlap ratio to the maximum number of pixels
 *
 * @author Owen Feehan
 */
public class OverlapRelativeToMaxVolume extends OverlapRelative {

    @Override
    protected int calcDenominator(FeatureInputPairObjects params) {
        return OverlapRatioUtilities.denominatorMaxVolume(params);
    }
}
