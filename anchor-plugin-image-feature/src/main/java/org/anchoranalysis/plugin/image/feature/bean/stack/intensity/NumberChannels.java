package org.anchoranalysis.plugin.image.feature.bean.stack.intensity;

import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.cache.SessionInput;
import org.anchoranalysis.image.feature.bean.FeatureStack;
import org.anchoranalysis.image.feature.input.FeatureInputStack;


/**
 * The number of channels in an image-stack.
 * 
 * @author Owen Feehan
 *
 */
public class NumberChannels extends FeatureStack {

    @Override
    protected double calculate(SessionInput<FeatureInputStack> input)
            throws FeatureCalculationException {
        return input.get().getEnergyStackRequired().getNumberChannels();
    }
}
