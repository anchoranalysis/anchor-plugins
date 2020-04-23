package org.anchoranalysis.plugin.image.feature.obj.pair;

import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.feature.cache.calculation.CachedCalculation;
import org.anchoranalysis.feature.cache.calculation.RslvdCachedCalculation;
import org.anchoranalysis.feature.calc.params.FeatureInput;

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
public abstract class CalculateParamsFromDelegate<S, T extends FeatureInput, U> extends CachedCalculation<S, T> {

	private RslvdCachedCalculation<U, T> ccDelegate;

	protected CalculateParamsFromDelegate(RslvdCachedCalculation<U, T> ccDelegate) {
		super();
		this.ccDelegate = ccDelegate;
	}

	@Override
	public S execute(T params) throws ExecuteException {
		
		U delegate = ccDelegate.getOrCalculate(params);
		return deriveFromDelegate(params, delegate);
	}
	
	protected abstract S deriveFromDelegate( T params, U delegate);

	protected RslvdCachedCalculation<U, T> getDelegate() {
		return ccDelegate;
	}
}
