package org.anchoranalysis.plugin.image.feature.bean.obj.intersecting;

import org.anchoranalysis.feature.cache.calculation.RslvdCachedCalculation;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.feature.objmask.pair.FeatureInputPairObjs;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.plugin.image.feature.obj.pair.CalculateParamsFromDelegate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class CalculateIntersecting extends CalculateParamsFromDelegate<FeatureInputPairObjs, FeatureInputSingleObj, ObjMaskCollection> {

	private int index;
	
	public CalculateIntersecting(RslvdCachedCalculation<ObjMaskCollection, FeatureInputSingleObj> intersecting, int index) {
		super(intersecting);
		this.index = index;
	}
	
	@Override
	protected FeatureInputPairObjs deriveFromDelegate(FeatureInputSingleObj params, ObjMaskCollection delegate) {
		return new FeatureInputPairObjs(
			params.getObjMask(),
			delegate.get(index),
			params.getNrgStack()
		);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) { return false; }
		if (obj == this) { return true; }
		if (obj.getClass() != getClass()) {
			return false;
		}
		CalculateIntersecting rhs = (CalculateIntersecting) obj;
		return new EqualsBuilder()
             .append(index, rhs.index)
             .append(getDelegate(), rhs.getDelegate())
             .isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(index)
			.append(getDelegate().hashCode())
			.toHashCode();
	}
}
