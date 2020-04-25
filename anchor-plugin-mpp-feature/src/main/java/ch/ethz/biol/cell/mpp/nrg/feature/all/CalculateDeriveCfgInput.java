package ch.ethz.biol.cell.mpp.nrg.feature.all;

import org.anchoranalysis.anchor.mpp.feature.bean.cfg.FeatureInputCfg;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputAllMemo;
import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.feature.cache.calculation.CacheableCalculation;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class CalculateDeriveCfgInput extends CacheableCalculation<FeatureInputCfg, FeatureInputAllMemo> {

	@Override
	protected FeatureInputCfg execute(FeatureInputAllMemo input) throws ExecuteException {
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
