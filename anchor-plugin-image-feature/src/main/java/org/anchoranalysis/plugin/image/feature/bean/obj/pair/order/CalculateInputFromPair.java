package org.anchoranalysis.plugin.image.feature.bean.obj.pair.order;

import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.feature.objmask.pair.FeatureInputPairObjs;
import org.anchoranalysis.image.objmask.ObjMask;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


public class CalculateInputFromPair extends FeatureCalculation<FeatureInputSingleObj, FeatureInputPairObjs> {

	private boolean first;
	
	/**
	 * Constructor
	 * 
	 * @param first iff-true the first object of the pair is used for creating params, otherwise the second object 
	 */
	public CalculateInputFromPair(boolean first) {
		super();
		this.first = first;
	}

	@Override
	protected FeatureInputSingleObj execute(FeatureInputPairObjs input) {
		FeatureInputSingleObj paramsNew = new FeatureInputSingleObj(
			extractObj(input)
		);
		paramsNew.setNrgStack( input.getNrgStackOptional() );
		return paramsNew;
	}
	
	private ObjMask extractObj(FeatureInputPairObjs input) {
		return first ? input.getLeft() : input.getRight();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) { return false; }
		if (obj == this) { return true; }
		if (obj.getClass() != getClass()) {
			return false;
		}
		CalculateInputFromPair rhs = (CalculateInputFromPair) obj;
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
