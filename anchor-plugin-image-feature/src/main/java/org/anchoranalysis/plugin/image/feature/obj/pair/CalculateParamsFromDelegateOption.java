package org.anchoranalysis.plugin.image.feature.obj.pair;

import java.util.Optional;
import java.util.function.Function;

import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.CacheableParams;
import org.anchoranalysis.feature.cache.calculation.CachedCalculation;
import org.anchoranalysis.feature.cache.calculation.RslvdCachedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.params.FeatureInput;
import org.apache.commons.lang.builder.EqualsBuilder;
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
public abstract class CalculateParamsFromDelegateOption<S extends FeatureInput, T extends FeatureInput, U> extends CalculateParamsFromDelegate<Optional<S>, T, U> {
	
	/**
	 * The constructor
	 * 
	 * @param ccDelegate the calculated-calculation for the delegate
	 */
	protected CalculateParamsFromDelegateOption(RslvdCachedCalculation<U, T> ccDelegate) {
		super(ccDelegate);
	}
	
	/**
	 * Calculates a feature using a cached-calculation as delegate, and optional Params return value
	 * 
	 * @param <S> optional final params-type for calculation
	 * @param <T> input params-type for calculation
	 * @param <U> delegate-type of CachedCalculation
	 * @param params input params
	 * @param delegate the Cached-Calculation delegate used
	 * @param funcCreateFromDelegate creates output-params from the delegates
	 * @param feature feature to use to calculate the output result
	 * @param cacheName a sub-cache of the main cache to use for calculating the output feature
	 * @param emptyValue what to return if the parameters are empty
	 * @return the result of the feature calculation
	 * @throws FeatureCalcException
	 */
	public static <S extends FeatureInput, T extends FeatureInput, U> double calc(
		CacheableParams<T> params,
		CachedCalculation<U,T> delegate,
		Function<RslvdCachedCalculation<U,T>,CalculateParamsFromDelegateOption<S,T,U>> funcCreateFromDelegate,
		Feature<S> feature,
		String cacheName,
		double emptyValue
	) throws FeatureCalcException {

		CachedCalculation<Optional<S>,T> ccParamsDerived =
			funcCreateFromDelegate.apply(
				params.search(delegate)
			);
		
		Optional<S> paramsDerived = params.calc(ccParamsDerived);
		
		if (!paramsDerived.isPresent()) {
			return emptyValue;
		}
		
		// We select an appropriate cache for calculating the feature (should be the same as selected in init())
		return params.calcChangeParamsDirect(
			feature,
			paramsDerived.get(),
			cacheName
		);
	}
	
	/*@Override
	public boolean equals(Object other) {
	    return (clssCalculateParams.isAssignableFrom(other.getClass()));
	}*/

	/*@Override
	public int hashCode() {
		return new HashCodeBuilder().toHashCode();
	}*/
	
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (obj == null) { return false; }
		if (obj == this) { return true; }
		if (obj.getClass() != getClass()) {
			return false;
		}
		CalculateParamsFromDelegateOption<S,T,U> rhs = (CalculateParamsFromDelegateOption<S,T,U>) obj;
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
