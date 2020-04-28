package org.anchoranalysis.plugin.image.feature.bean.obj.pair.order;

import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.feature.objmask.pair.FeatureInputPairObjs;
import org.anchoranalysis.image.objmask.ObjMask;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


/**
 * Calculates a single-input from a pair
 * 
 * <div>
 * Three states are possible:
 * <ul>
 * <li>First</li>
 * <li>Second</li>
 * <li>Merged</li>
 * </div>
 * 
 * @author Owen Feehan
 *
 */
public class CalculateInputFromPair extends FeatureCalculation<FeatureInputSingleObj, FeatureInputPairObjs> {

	public enum Extract {
		FIRST,
		SECOND,
		MERGED
	}
	
	private Extract extract;
	
	/**
	 * Constructor
	 * 
	 * @param merged iff-true the merged object of the pair is used (and the parameter first is ignored)
	 * 	@param firstOrSecond iff-true the first object of the pair is used for creating params, otherwise the second object 
	 */
	public CalculateInputFromPair(Extract extract) {
		super();
		this.extract = extract;
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
		
		if (extract==Extract.MERGED) {
			return input.getMerged();
		}
		
		return extract==Extract.FIRST ? input.getFirst() : input.getSecond();
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
             .append(extract, rhs.extract)
             .isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(extract)
			.toHashCode();
	}

}
