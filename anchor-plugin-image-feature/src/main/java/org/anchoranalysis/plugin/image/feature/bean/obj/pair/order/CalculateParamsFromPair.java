package org.anchoranalysis.plugin.image.feature.bean.obj.pair.order;

import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.feature.cache.calculation.CacheableCalculation;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.feature.objmask.pair.FeatureInputPairObjs;
import org.anchoranalysis.image.objmask.ObjMask;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


class CalculateParamsFromPair extends CacheableCalculation<FeatureInputSingleObj, FeatureInputPairObjs> {

	private boolean first;
	
	/**
	 * Constructor
	 * 
	 * @param first iff-true the first object of the pair is used for creating params, otherwise the second object 
	 */
	public CalculateParamsFromPair(boolean first) {
		super();
		this.first = first;
	}

	@Override
	protected FeatureInputSingleObj execute(FeatureInputPairObjs params) throws ExecuteException {
		FeatureInputSingleObj paramsNew = new FeatureInputSingleObj(
			extractObj(params)
		);
		paramsNew.setNrgStack( params.getNrgStack() );
		return paramsNew;
	}
	
	private ObjMask extractObj(FeatureInputPairObjs params) {
		return first ? params.getObjMask1() : params.getObjMask2();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) { return false; }
		if (obj == this) { return true; }
		if (obj.getClass() != getClass()) {
			return false;
		}
		CalculateParamsFromPair rhs = (CalculateParamsFromPair) obj;
		return new EqualsBuilder()
             .append(first, rhs.first)
             .isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(first)
			.toHashCode();
	}

}
