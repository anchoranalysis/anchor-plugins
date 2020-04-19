package org.anchoranalysis.plugin.image.feature.obj.pair;

import java.util.Optional;

import org.anchoranalysis.feature.cachedcalculation.CachedCalculation;
import org.anchoranalysis.feature.cachedcalculation.RslvdCachedCalculation;
import org.anchoranalysis.feature.calc.params.FeatureCalcParams;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Like {@link #CalculateParamsFromDelegateOption(CachedCalculation) except assumes optional return value and no parameters
 * 
 * @author Owen Feehan
 *
 * @param <S> optional final-type of CachedCalculation
 * @param <T> params-type as input to cached-calculations
 * @param <U> delegate-type of CachedCalculation
 */
public abstract class CalculateParamsFromDelegateOption<S, T extends FeatureCalcParams, U> extends CalculateParamsFromDelegate<Optional<S>, T, U> {

	private Class<?> clssCalculateParams;
	
	/**
	 * The constructor
	 * 
	 * @param ccDelegate the calculated-calculation for the delegate
	 * @param clssCalculateParams the lowest-level class for the CalculateParams (for checking equality assuming no parameters)
	 */
	protected CalculateParamsFromDelegateOption(RslvdCachedCalculation<U, T> ccDelegate, Class<?> clssCalculateParams) {
		super(ccDelegate);
		this.clssCalculateParams = clssCalculateParams;
	}
	
	@Override
	public boolean equals(Object other) {
	    return (clssCalculateParams.isAssignableFrom(other.getClass()));
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().toHashCode();
	}

}
