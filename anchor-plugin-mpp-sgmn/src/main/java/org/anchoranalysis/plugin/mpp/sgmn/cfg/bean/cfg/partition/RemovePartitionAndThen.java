/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.cfg.partition;

import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.mpp.sgmn.transformer.Compose;
import org.anchoranalysis.mpp.sgmn.transformer.StateTransformerBean;
import org.anchoranalysis.mpp.sgmn.transformer.TransformationContext;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.optscheme.CfgFromPartition;

public class RemovePartitionAndThen<T> extends StateTransformerBean<CfgFromPartition, T> {

    // START BEAN PROPERTIES
    @BeanField private StateTransformerBean<Cfg, T> transformer;
    // END BEAN PROPERTIES

    @Override
    public T transform(CfgFromPartition in, TransformationContext context)
            throws OperationFailedException {
        return createComposer().transform(in, context);
    }

    private Compose<CfgFromPartition, T, Cfg> createComposer() {
        Compose<CfgFromPartition, T, Cfg> compose = new Compose<>();
        compose.setFirst(new RemovePartition());
        compose.setSecond(transformer);
        return compose;
    }

    public StateTransformerBean<Cfg, T> getTransformer() {
        return transformer;
    }

    public void setTransformer(StateTransformerBean<Cfg, T> transformer) {
        this.transformer = transformer;
    }
}
