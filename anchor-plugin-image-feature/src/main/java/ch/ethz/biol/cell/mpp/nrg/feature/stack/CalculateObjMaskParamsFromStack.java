package ch.ethz.biol.cell.mpp.nrg.feature.stack;

import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.feature.cache.calculation.CacheableCalculation;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.feature.stack.FeatureInputStack;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

class CalculateObjMaskParamsFromStack extends CacheableCalculation<FeatureInputSingleObj, FeatureInputStack> {

	private ObjMaskCollection objs;
	private int index;
	
	
	/**
	 * Constructor
	 * 
	 * @param objs the object-mask collection to calculate from (ignored in hash-coding and equality as assumed to be singular)
	 * @param index index of particular object in objs to derive parameters for
	 */
	public CalculateObjMaskParamsFromStack(ObjMaskCollection objs, int index) {
		super();
		this.objs = objs;
		this.index = index;
	}

	@Override
	protected FeatureInputSingleObj execute(FeatureInputStack input) throws ExecuteException {
		return new FeatureInputSingleObj(
			objs.get(index),
			input.getNrgStackOptional()
		);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) { return false; }
		if (obj == this) { return true; }
		if (obj.getClass() != getClass()) {
			return false;
		}
		CalculateObjMaskParamsFromStack rhs = (CalculateObjMaskParamsFromStack) obj;
		return new EqualsBuilder()
             .append(index, rhs.index)
             .isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(index)
			.toHashCode();
	}

}
