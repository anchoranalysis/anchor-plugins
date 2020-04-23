package ch.ethz.biol.cell.mpp.nrg.feature.all;

import org.anchoranalysis.anchor.mpp.feature.bean.cfg.FeatureCfgParams;
import org.anchoranalysis.anchor.mpp.feature.nrg.elem.NRGElemAllCalcParams;
import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.feature.cache.calculation.CachedCalculation;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class CalculateDeriveCfgParams extends CachedCalculation<FeatureCfgParams, NRGElemAllCalcParams> {

	@Override
	protected FeatureCfgParams execute(NRGElemAllCalcParams params) throws ExecuteException {
		return new FeatureCfgParams(
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
