package ch.ethz.biol.cell.mpp.nrg.feature.all;

import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureMarkParams;
import org.anchoranalysis.anchor.mpp.feature.mark.MemoMarks;
import org.anchoranalysis.anchor.mpp.feature.nrg.elem.NRGElemAllCalcParams;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.feature.cache.calculation.CachedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class CalculateDeriveMarkParams extends CachedCalculation<FeatureMarkParams, NRGElemAllCalcParams> {

	@Override
	protected FeatureMarkParams execute(NRGElemAllCalcParams params) throws ExecuteException {
		
		MemoMarks list = params.getPxlPartMemo();
		
		if (list.size()==0) {
			throw new ExecuteException(
				new FeatureCalcException("No mark exists in the list")
			);
		}
		
		if (list.size()>1) {
			throw new ExecuteException(
				new FeatureCalcException("More than one mark exists in the list")
			);
		}
		
		Mark mark = list.getMemoForIndex(0).getMark();
		
		return new FeatureMarkParams(
			mark,
			params.getDimensions().getRes()
		);	
	}
	
	@Override
	public boolean equals(Object other) {
		return (other instanceof CalculateDeriveMarkParams);
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().toHashCode();
	}
}
