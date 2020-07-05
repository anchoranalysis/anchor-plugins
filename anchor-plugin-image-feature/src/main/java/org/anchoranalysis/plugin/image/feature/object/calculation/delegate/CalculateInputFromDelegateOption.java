package org.anchoranalysis.plugin.image.feature.object.calculation.delegate;

/*-
 * #%L
 * anchor-plugin-image-feature
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan
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

import java.util.Optional;
import java.util.function.Function;

import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.ChildCacheName;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.cache.calculation.CacheableCalculation;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Like {@link #CalculateParamsFromDelegateOption(CacheableCalculation) except assumes optional return value and no parameters
 * 
 * @author Owen Feehan
 *
 * @param <S> optional final-type of CachedCalculation
 * @param <T> feature input-type as input to cached-calculations
 * @param <U> delegate-type of CachedCalculation
 */
public abstract class CalculateInputFromDelegateOption<S extends FeatureInput, T extends FeatureInput, U> extends CalculateInputFromDelegate<Optional<S>, T, U> {
	
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
		FeatureCalculation<U,T> delegate,
		Function<ResolvedCalculation<U,T>,CalculateInputFromDelegateOption<S,T,U>> funcCreateFromDelegate,
		Feature<S> feature,
		ChildCacheName cacheName,
		double emptyValue
	) throws FeatureCalcException {

		FeatureCalculation<Optional<S>,T> ccParamsDerived =
			funcCreateFromDelegate.apply(
				input.resolver().search(delegate)
			);
		
		Optional<S> inputForDelegate = input.calc(ccParamsDerived);
		
		if (!inputForDelegate.isPresent()) {
			return emptyValue;
		}
		
		// We select an appropriate cache for calculating the feature (should be the same as selected in init())
		return input.forChild().calc(
			feature,
			inputForDelegate.get(),
			cacheName
		);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (obj == null) { return false; }
		if (obj == this) { return true; }
		if (obj.getClass() != getClass()) {
			return false;
		}
		CalculateInputFromDelegateOption<S,T,U> rhs = (CalculateInputFromDelegateOption<S,T,U>) obj;
		return new EqualsBuilder()
             .append(getDelegate(), rhs.getDelegate())
             .isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(getDelegate())
			.toHashCode();
	}

}
