package ch.ethz.biol.cell.mpp.nrg.feature.ind;

import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureInputMark;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputSingleMemo;
import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.feature.cache.calculation.CacheableCalculation;

class CalculateDeriveMarkFromMemo extends CacheableCalculation<FeatureInputMark, FeatureInputSingleMemo> {

	@Override
	protected FeatureInputMark execute(FeatureInputSingleMemo input) throws ExecuteException {
		return new FeatureInputMark(
			input.getPxlPartMemo().getMark(),
			input.getDimensions().getRes()
		);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof CalculateDeriveMarkFromMemo;
	}

	@Override
	public int hashCode() {
		return 21;		// Arbitrary
	}
}
