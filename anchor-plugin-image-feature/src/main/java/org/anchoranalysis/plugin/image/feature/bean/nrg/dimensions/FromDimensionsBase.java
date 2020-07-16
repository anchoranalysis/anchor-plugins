/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.nrg.dimensions;

import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInputNRG;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.feature.bean.FeatureNRGStack;

/**
 * @author Owen Feehan
 * @param <T> feature-input-type
 */
public abstract class FromDimensionsBase<T extends FeatureInputNRG> extends FeatureNRGStack<T> {

    @Override
    public double calc(SessionInput<T> input) throws FeatureCalcException {
        return calcFromDims(input.get().getDimensionsRequired());
    }

    protected abstract double calcFromDims(ImageDimensions dimensions);
}
