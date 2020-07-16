/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.object.calculation.delegate;

import java.util.Optional;
import java.util.function.Function;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.ChildCacheName;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.cache.calculation.CacheableCalculation;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;

/**
 * Like {@link #CalculateParamsFromDelegateOption(CacheableCalculation) except assumes optional return value and no parameters
 *
 * @author Owen Feehan
 *
 * @param <S> optional final-type of CachedCalculation
 * @param <T> feature input-type as input to cached-calculations
 * @param <U> delegate-type of CachedCalculation
 */
@EqualsAndHashCode(callSuper = true)
public abstract class CalculateInputFromDelegateOption<
                S extends FeatureInput, T extends FeatureInput, U>
        extends CalculateInputFromDelegate<Optional<S>, T, U> {

    /**
     * The constructor
     *
     * @param ccDelegate the calculated-calculation for the delegate
     */
    protected CalculateInputFromDelegateOption(ResolvedCalculation<U, T> ccDelegate) {
        super(ccDelegate);
    }

    /**
     * Calculates a feature using a cached-calculation as delegate, and optional Params return value
     *
     * @param <S> optional final params-type for calculation
     * @param <T> input params-type for calculation
     * @param <U> delegate-type of CachedCalculation
     * @param input feature-input
     * @param delegate the Cached-Calculation delegate used
     * @param funcCreateFromDelegate creates output-params from the delegates
     * @param feature feature to use to calculate the output result
     * @param cacheName a sub-cache of the main cache to use for calculating the output feature
     * @param emptyValue what to return if the parameters are empty
     * @return the result of the feature calculation
     * @throws FeatureCalcException
     */
    public static <S extends FeatureInput, T extends FeatureInput, U> double calc(
            SessionInput<T> input,
            FeatureCalculation<U, T> delegate,
            Function<ResolvedCalculation<U, T>, CalculateInputFromDelegateOption<S, T, U>>
                    funcCreateFromDelegate,
            Feature<S> feature,
            ChildCacheName cacheName,
            double emptyValue)
            throws FeatureCalcException {

        FeatureCalculation<Optional<S>, T> ccParamsDerived =
                funcCreateFromDelegate.apply(input.resolver().search(delegate));

        Optional<S> inputForDelegate = input.calc(ccParamsDerived);

        if (!inputForDelegate.isPresent()) {
            return emptyValue;
        }

        // We select an appropriate cache for calculating the feature (should be the same as
        // selected in init())
        return input.forChild().calc(feature, inputForDelegate.get(), cacheName);
    }
}
