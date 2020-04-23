package ch.ethz.biol.cell.mpp.nrg.feature.pair;

import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputPairMemo;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputSingleMemo;
import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.feature.cache.calculation.CachedCalculation;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

class CalculateDeriveSingleInputFromPair extends CachedCalculation<FeatureInputSingleMemo, FeatureInputPairMemo> {

	/** Iff TRUE, then the first object from the pair is used, otherwise the second is */
	private boolean first;

	public CalculateDeriveSingleInputFromPair(boolean first) {
		super();
		this.first = first;
	}

	@Override
	protected FeatureInputSingleMemo execute(FeatureInputPairMemo params) throws ExecuteException {
		return new FeatureInputSingleMemo(
			first ? params.getObj1() : params.getObj2(),
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
		CalculateDeriveSingleInputFromPair rhs = (CalculateDeriveSingleInputFromPair) obj;
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
