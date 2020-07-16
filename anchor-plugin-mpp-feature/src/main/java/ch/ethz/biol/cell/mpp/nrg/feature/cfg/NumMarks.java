/* (C)2020 */
package ch.ethz.biol.cell.mpp.nrg.feature.cfg;

import org.anchoranalysis.anchor.mpp.feature.bean.cfg.FeatureCfg;
import org.anchoranalysis.anchor.mpp.feature.bean.cfg.FeatureInputCfg;
import org.anchoranalysis.feature.calc.FeatureCalcException;

public class NumMarks extends FeatureCfg {

    @Override
    public double calc(FeatureInputCfg params) throws FeatureCalcException {
        return params.getCfg().size();
    }
}
