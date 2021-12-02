/*-
 * #%L
 * anchor-plugin-image-feature
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

package org.anchoranalysis.plugin.image.feature.object.calculation.delegate;

import java.util.Optional;
import java.util.function.Function;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.FeatureCalculationInput;
import org.anchoranalysis.feature.calculate.cache.ChildCacheName;
import org.anchoranalysis.feature.calculate.cache.part.ResolvedPart;
import org.anchoranalysis.feature.calculate.part.CalculationPart;
import org.anchoranalysis.feature.input.FeatureInput;

/**
 * Like {@link CalculateInputFromDelegate} except assumes optional return value and no parameters.
 *
 * @author Owen Feehan
 * @param <S> optional final-type of {@link CalculationPart}.
 * @param <T> feature input-type as input to cached-calculations.
 * @param <U> delegate-type of {@link CalculationPart}.
 */
@EqualsAndHashCode(callSuper = true)
public abstract class CalculateInputFromDelegateOption<
                S extends FeatureInput, T extends FeatureInput, U>
        extends CalculateInputFromDelegate<Optional<S>, T, U> {

    /**
     * Creates with a delegate.
     *
     * @param delegate the resolved-calculation for the delegate.
     */
    protected CalculateInputFromDelegateOption(ResolvedPart<U, T> delegate) {
        super(delegate);
    }

    /**
     * Calculates a feature using a cached-calculation as delegate.
     *
     * @param <S> optional final input-type for calculation
     * @param <T> input-type for calculation
     * @param <U> delegate-type of CachedCalculation
     * @param input feature-input.
     * @param delegate the Cached-Calculation delegate used.
     * @param createFromDelegate creates a calculation from the delegates.
     * @param feature feature to use to calculate the output result.
     * @param cacheName a sub-cache of the main cache to use for calculating the output feature.
     * @param emptyValue what to return if the parameters are empty.
     * @return the result of the feature calculation.
     * @throws FeatureCalculationException if the calculation does not succeed successfully.
     */
    public static <S extends FeatureInput, T extends FeatureInput, U> double calc(
            FeatureCalculationInput<T> input,
            CalculationPart<U, T> delegate,
            Function<ResolvedPart<U, T>, CalculateInputFromDelegateOption<S, T, U>>
                    createFromDelegate,
            Feature<S> feature,
            ChildCacheName cacheName,
            double emptyValue)
            throws FeatureCalculationException {

        CalculationPart<Optional<S>, T> inputDerived =
                createFromDelegate.apply(input.resolver().search(delegate));

        Optional<S> inputForDelegate = input.calculate(inputDerived);

        if (inputForDelegate.isPresent()) {
            // We select an appropriate cache for calculating the feature (should be the same as
            // selected in initialize())
            return input.forChild().calculate(feature, inputForDelegate.get(), cacheName);
        } else {
            return emptyValue;
        }
    }
}
