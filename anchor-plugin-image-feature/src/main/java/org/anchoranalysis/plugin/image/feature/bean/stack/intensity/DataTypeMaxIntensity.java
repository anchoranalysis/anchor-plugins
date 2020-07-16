/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.stack.intensity;

import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInputNRG;
import org.anchoranalysis.image.feature.bean.FeatureNRGStack;

/**
 * The maximum-intensity allowed by the data-type of the stack (e.g. 255 for unsigned 8-bit).
 *
 * <p>Note this is NOT the actual max-intensity seen in the stack, rather the theoretical max of the
 * data-type.
 *
 * @author Owen Feehan
 * @param <T> feature-input-type
 */
public class DataTypeMaxIntensity<T extends FeatureInputNRG> extends FeatureNRGStack<T> {

    @Override
    public double calc(SessionInput<T> input) throws FeatureCalcException {
        return (double) input.get().getNrgStackRequired().getChnl(0).getVoxelDataType().maxValue();
    }
}
