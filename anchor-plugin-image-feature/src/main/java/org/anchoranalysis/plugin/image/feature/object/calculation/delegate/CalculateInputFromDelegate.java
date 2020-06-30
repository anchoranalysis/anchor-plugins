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

import org.anchoranalysis.feature.cache.calculation.CalculationResolver;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;

/**
 * A base class for Cached-Calculations that generate a Params for feature-calculation using an existing "delegate" calculation
 * 
 * <p>
 * These types of calculations involve two steps:
 * <ul>
 * <li>Calculating from an existing cached-calculation</li>
 * <li>Applying a transform to generate parameters</li>
 * </ul>
 * </p>
 * 
 * @author Owen Feehan
 *
 * @param <S> final-type of CachedCalculation
 * @param <T> feature input-type as input to cached-calculations
 * @param <U> delegate-type of CachedCalculation
 * 
 */
public abstract class CalculateInputFromDelegate<S, T extends FeatureInput, U> extends FeatureCalculation<S, T> {

	private ResolvedCalculation<U, T> ccDelegate;

	protected CalculateInputFromDelegate(ResolvedCalculation<U, T> ccDelegate) {
		super();
		this.ccDelegate = ccDelegate;
	}
	
	protected CalculateInputFromDelegate(FeatureCalculation<U, T> ccDelegate, CalculationResolver<T> cache) {
		super();
		this.ccDelegate = cache.search(ccDelegate);
	}

	@Override
	public S execute(T input) throws FeatureCalcException {
		
		U delegate = ccDelegate.getOrCalculate(input);
		return deriveFromDelegate(input, delegate);
	}
	
	protected abstract S deriveFromDelegate( T input, U delegate);

	protected ResolvedCalculation<U, T> getDelegate() {
		return ccDelegate;
	}
}
