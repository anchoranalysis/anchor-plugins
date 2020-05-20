package org.anchoranalysis.plugin.points.calculate.ellipsoid;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.functional.Operation;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;

/**
 * Binds params with a CachedCalculation and exposes it as the {@link org.anchoranalysis.core.functional.Operation} interface
 * 
 * (reverse-currying)
 * 
 * @author Owen Feehan
 *
 * @param <S> result-type
 * @param <T> params-type
 */
class CachedCalculationOperation<S, T extends FeatureInput> implements Operation<S, CreateException> {

	private ResolvedCalculation<S,T> cachedCalculation;
	private T params;
		
	public CachedCalculationOperation(ResolvedCalculation<S,T> cachedCalculation, T params) {
		super();
		this.cachedCalculation = cachedCalculation;
		this.params = params;
	}
	
	@Override
	public S doOperation() throws CreateException {
		try {
			return cachedCalculation.getOrCalculate(params);
		} catch (FeatureCalcException e) {
			throw new CreateException(e);
		}
	}
}
