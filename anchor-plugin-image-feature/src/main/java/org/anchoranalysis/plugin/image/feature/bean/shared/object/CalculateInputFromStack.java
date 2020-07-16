/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.shared.object;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.input.FeatureInputNRG;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectCollection;

/**
 * Calculates an input for a single-object with an optional NRG-stack from the feature-input
 *
 * @author Owen Feehan
 * @param <T> feature-input
 */
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
class CalculateInputFromStack<T extends FeatureInputNRG>
        extends FeatureCalculation<FeatureInputSingleObject, T> {

    /**
     * The object-mask collection to calculate from (ignored in hash-coding and equality as assumed
     * to be singular)
     */
    private final ObjectCollection objects;

    /** Index of particular object in objects to derive parameters for */
    private final int index;

    @Override
    protected FeatureInputSingleObject execute(T input) {
        return new FeatureInputSingleObject(objects.get(index), input.getNrgStackOptional());
    }
}
