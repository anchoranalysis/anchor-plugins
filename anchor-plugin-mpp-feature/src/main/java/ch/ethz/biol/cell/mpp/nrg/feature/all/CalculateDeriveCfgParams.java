package ch.ethz.biol.cell.mpp.nrg.feature.all;

import org.anchoranalysis.anchor.mpp.feature.bean.cfg.FeatureInputCfg;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputAllMemo;
import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.feature.cache.calculation.CachedCalculation;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class CalculateDeriveCfgParams extends CachedCalculation<FeatureInputCfg, FeatureInputAllMemo> {

	@Override
	protected FeatureInputCfg execute(FeatureInputAllMemo params) throws ExecuteException {
		return new FeatureInputCfg(
			params.getPxlPartMemo().asCfg(),
			params.getDimensions()
		);
	}
	
	@Override
	public boolean equals(Object other) {
		return (other instanceof CalculateDeriveCfgParams);
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().toHashCode();
	}
}
