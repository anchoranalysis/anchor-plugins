/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.object.calculation.delegate;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.anchoranalysis.feature.cache.calculation.CalculationResolver;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;

/**
 * A base class for Cached-Calculations that generate a Params for feature-calculation using an
 * existing "delegate" calculation
 *
 * <p>These types of calculations involve two steps:
 *
 * <ul>
 *   <li>Calculating from an existing cached-calculation
 *   <li>Applying a transform to generate parameters
 * </ul>
 *
 * @author Owen Feehan
 * @param <S> final-type of CachedCalculation
 * @param <T> feature input-type as input to cached-calculations
 * @param <U> delegate-type of CachedCalculation
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
public abstract class CalculateInputFromDelegate<S, T extends FeatureInput, U>
        extends FeatureCalculation<S, T> {

    private final ResolvedCalculation<U, T> ccDelegate;

    protected CalculateInputFromDelegate(
            FeatureCalculation<U, T> ccDelegate, CalculationResolver<T> cache) {
        this(cache.search(ccDelegate));
    }

    @Override
    public S execute(T input) throws FeatureCalcException {

        U delegate = ccDelegate.getOrCalculate(input);
        return deriveFromDelegate(input, delegate);
    }

    protected abstract S deriveFromDelegate(T input, U delegate);

    protected ResolvedCalculation<U, T> getDelegate() {
        return ccDelegate;
    }
}
