/* (C)2020 */
package ch.ethz.biol.cell.mpp.nrg.feature.all;

import lombok.EqualsAndHashCode;
import org.anchoranalysis.anchor.mpp.feature.bean.cfg.FeatureInputCfg;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputAllMemo;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;

@EqualsAndHashCode(callSuper = false)
public class CalculateDeriveCfgInput
        extends FeatureCalculation<FeatureInputCfg, FeatureInputAllMemo> {

    @Override
    protected FeatureInputCfg execute(FeatureInputAllMemo input) throws FeatureCalcException {
        return new FeatureInputCfg(input.getPxlPartMemo().asCfg(), input.getDimensionsOptional());
    }
}
