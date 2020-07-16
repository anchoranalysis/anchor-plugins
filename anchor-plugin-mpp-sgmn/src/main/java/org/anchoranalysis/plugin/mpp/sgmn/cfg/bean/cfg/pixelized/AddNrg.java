/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.cfg.pixelized;

import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.mpp.sgmn.transformer.StateTransformerBean;
import org.anchoranalysis.mpp.sgmn.transformer.TransformationContext;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.optscheme.CfgNRGPixelizedFactory;

public class AddNrg extends StateTransformerBean<Cfg, CfgNRGPixelized> {

    @Override
    public CfgNRGPixelized transform(Cfg in, TransformationContext context)
            throws OperationFailedException {
        try {
            return CfgNRGPixelizedFactory.createFromCfg(
                    in, context.getKernelCalcContext(), context.getLogger());
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }
}
