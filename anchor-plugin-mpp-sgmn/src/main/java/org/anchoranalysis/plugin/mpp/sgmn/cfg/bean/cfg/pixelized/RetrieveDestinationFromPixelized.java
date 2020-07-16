/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.cfg.pixelized;

import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.mpp.sgmn.transformer.StateTransformerBean;
import org.anchoranalysis.mpp.sgmn.transformer.TransformationContext;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.optscheme.ToPixelized;

public class RetrieveDestinationFromPixelized<T>
        extends StateTransformerBean<ToPixelized<T>, CfgNRGPixelized> {

    @Override
    public CfgNRGPixelized transform(ToPixelized<T> in, TransformationContext context)
            throws OperationFailedException {
        return in.getDest();
    }
}
