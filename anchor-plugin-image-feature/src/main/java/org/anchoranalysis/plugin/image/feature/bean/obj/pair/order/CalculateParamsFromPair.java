package org.anchoranalysis.plugin.image.feature.bean.obj.pair.order;

import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.feature.cache.calculation.CachedCalculation;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.feature.objmask.pair.FeatureObjMaskPairParams;
import org.anchoranalysis.image.objmask.ObjMask;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


class CalculateParamsFromPair extends CachedCalculation<FeatureObjMaskParams, FeatureObjMaskPairParams> {

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
	protected FeatureObjMaskParams execute(FeatureObjMaskPairParams params) throws ExecuteException {
		FeatureObjMaskParams paramsNew = new FeatureObjMaskParams(
			extractObj(params)
		);
		paramsNew.setNrgStack( params.getNrgStack() );
		return paramsNew;
	}
	
	private ObjMask extractObj(FeatureObjMaskPairParams params) {
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
