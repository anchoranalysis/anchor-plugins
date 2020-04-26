package ch.ethz.biol.cell.mpp.nrg.feature.all;

import org.anchoranalysis.anchor.mpp.feature.bean.cfg.FeatureInputCfg;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputAllMemo;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class CalculateDeriveCfgInput extends FeatureCalculation<FeatureInputCfg, FeatureInputAllMemo> {

	@Override
	protected FeatureInputCfg execute(FeatureInputAllMemo input) throws FeatureCalcException {
		return new FeatureInputCfg(
			input.getPxlPartMemo().asCfg(),
			input.getDimensionsOptional()
		);
	}
	
	@Override
	public boolean equals(Object other) {
		return (other instanceof CalculateDeriveCfgInput);
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().toHashCode();
	}
}
