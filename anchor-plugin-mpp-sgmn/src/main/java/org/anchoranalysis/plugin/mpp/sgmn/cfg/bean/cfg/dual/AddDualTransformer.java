/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.cfg.dual;

import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.mpp.sgmn.transformer.StateTransformerBean;
import org.anchoranalysis.mpp.sgmn.transformer.TransformationContext;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.optscheme.DualCfgNRGPixelized;

public class AddDualTransformer extends StateTransformerBean<CfgNRGPixelized, DualCfgNRGPixelized> {

    // START BEAN PROPERTIES
    @BeanField private StateTransformerBean<Cfg, CfgNRGPixelized> transformerCfg;
    // END BEAN PROPERTIES

    @Override
    public DualCfgNRGPixelized transform(CfgNRGPixelized in, TransformationContext context)
            throws OperationFailedException {
        return new DualCfgNRGPixelized(in, transformerCfg.transform(in.getCfg(), context));
    }

    public StateTransformerBean<Cfg, CfgNRGPixelized> getTransformerCfg() {
        return transformerCfg;
    }

    public void setTransformerCfg(StateTransformerBean<Cfg, CfgNRGPixelized> transformerCfg) {
        this.transformerCfg = transformerCfg;
    }
}
