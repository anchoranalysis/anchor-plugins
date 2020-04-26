package org.anchoranalysis.plugin.image.feature.obj.pair;

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
	public S execute(T params) throws FeatureCalcException {
		
		U delegate = ccDelegate.getOrCalculate(params);
		return deriveFromDelegate(params, delegate);
	}
	
	protected abstract S deriveFromDelegate( T params, U delegate);

	protected ResolvedCalculation<U, T> getDelegate() {
		return ccDelegate;
	}
}
