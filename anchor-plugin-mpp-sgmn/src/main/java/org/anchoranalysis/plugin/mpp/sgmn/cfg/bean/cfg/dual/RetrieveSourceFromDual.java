/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.cfg.dual;

import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.mpp.sgmn.transformer.StateTransformerBean;
import org.anchoranalysis.mpp.sgmn.transformer.TransformationContext;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.optscheme.DualCfgNRGPixelized;

public class RetrieveSourceFromDual
        extends StateTransformerBean<DualCfgNRGPixelized, CfgNRGPixelized> {

    @Override
    public CfgNRGPixelized transform(DualCfgNRGPixelized in, TransformationContext context)
            throws OperationFailedException {
        return in.getSrc();
    }
}
