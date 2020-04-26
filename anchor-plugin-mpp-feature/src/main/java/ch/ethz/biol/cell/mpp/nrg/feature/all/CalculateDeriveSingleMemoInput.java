package ch.ethz.biol.cell.mpp.nrg.feature.all;

import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputAllMemo;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputSingleMemo;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

class CalculateDeriveSingleMemoInput extends FeatureCalculation<FeatureInputSingleMemo, FeatureInputAllMemo> {

	private int index;
		
	public CalculateDeriveSingleMemoInput(int index) {
		super();
		this.index = index;
	}

	@Override
	protected FeatureInputSingleMemo execute(FeatureInputAllMemo input) {
		FeatureInputSingleMemo paramsInd = new FeatureInputSingleMemo(
			null,
			input.getNrgStackOptional()
		);
		paramsInd.setPxlPartMemo(
			input.getPxlPartMemo().getMemoForIndex(index)
		);
		return paramsInd;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) { return false; }
		if (obj == this) { return true; }
		if (obj.getClass() != getClass()) {
			return false;
		}
		CalculateDeriveSingleMemoInput rhs = (CalculateDeriveSingleMemoInput) obj;
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
