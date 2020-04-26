package ch.ethz.biol.cell.mpp.nrg.feature.ind;

import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMap;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputSingleMemo;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.properties.ObjMaskWithProperties;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

class CalculateSingleObjFromMemo extends FeatureCalculation<FeatureInputSingleObj, FeatureInputSingleMemo> {

	private RegionMap regionMap;
	private int index;
		
	public CalculateSingleObjFromMemo(RegionMap regionMap, int index) {
		super();
		this.regionMap = regionMap;
		this.index = index;
	}

	@Override
	protected FeatureInputSingleObj execute(FeatureInputSingleMemo input) throws FeatureCalcException {
		return new FeatureInputSingleObj(
			calcMask(input),
			input.getNrgStackOptional()
		);
	}
	
	private ObjMask calcMask(FeatureInputSingleMemo params) throws FeatureCalcException {
		ObjMaskWithProperties om = params.getPxlPartMemo().getMark().calcMask(
			params.getDimensionsRequired(),
			regionMap.membershipWithFlagsForIndex(index),
			BinaryValuesByte.getDefault()
		);
		return om.getMask();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) { return false; }
		if (obj == this) { return true; }
		if (obj.getClass() != getClass()) {
			return false;
		}
		CalculateSingleObjFromMemo rhs = (CalculateSingleObjFromMemo) obj;
		return new EqualsBuilder()
             .append(regionMap, rhs.regionMap)
             .append(index, rhs.index)
             .isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(index)
			.append(regionMap)
			.toHashCode();
	}
}
