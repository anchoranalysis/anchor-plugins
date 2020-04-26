package ch.ethz.biol.cell.mpp.nrg.feature.objmaskpairmerged;

import java.util.function.Function;

import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.feature.objmask.pair.merged.FeatureInputPairObjsMerged;
import org.anchoranalysis.image.objmask.ObjMask;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

class CalculateDeriveSingleObjFromMerged extends FeatureCalculation<FeatureInputSingleObj, FeatureInputPairObjsMerged> {

	private Function<FeatureInputPairObjsMerged, ObjMask> extractObjFunc;
	private String uniqueIDForFunction;
	
	
	/**
	 * Constructor
	 *
	 * <p>uniqueIDForFunction should be a constant unique for each different extractObjFunc</p>
	 * 
	 * @param extractObjFunc this function is used for extracting a particular object from the FeatureObjMaskPairMergedParams
	 * @param uniqueIDForFunction so as to avoid relying on hashCode() and equals() on extractObjFunc, this field is used as a unique ID instead for each type of lambda
	 */
	public CalculateDeriveSingleObjFromMerged(Function<FeatureInputPairObjsMerged, ObjMask> extractObjFunc,
			String uniqueIDForFunction) {
		super();
		this.extractObjFunc = extractObjFunc;
		this.uniqueIDForFunction = uniqueIDForFunction;
	}

	@Override
	protected FeatureInputSingleObj execute(FeatureInputPairObjsMerged input) {
		
		ObjMask omSelected = extractObjFunc.apply(input);
		
		FeatureInputSingleObj paramsNew = new FeatureInputSingleObj( omSelected );
		paramsNew.setNrgStack( input.getNrgStackOptional() );
		assert( paramsNew instanceof FeatureInputSingleObj);
		return paramsNew;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) { return false; }
		if (obj == this) { return true; }
		if (obj.getClass() != getClass()) {
			return false;
		}
		CalculateDeriveSingleObjFromMerged rhs = (CalculateDeriveSingleObjFromMerged) obj;
		return new EqualsBuilder()
             .append(uniqueIDForFunction, rhs.uniqueIDForFunction)
             .isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(uniqueIDForFunction)
			.toHashCode();
	}
}
