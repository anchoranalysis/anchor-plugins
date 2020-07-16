/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.cfg.partition;

import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.mpp.sgmn.transformer.StateTransformerBean;
import org.anchoranalysis.mpp.sgmn.transformer.TransformationContext;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.optscheme.CfgFromPartition;

public class RemovePartition extends StateTransformerBean<CfgFromPartition, Cfg> {

    @Override
    public Cfg transform(CfgFromPartition in, TransformationContext context)
            throws OperationFailedException {
        return in.getCfg();
    }
}
