/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.cfg.pixelized;

import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.mpp.sgmn.transformer.StateTransformerBean;
import org.anchoranalysis.mpp.sgmn.transformer.TransformationContext;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.optscheme.ToPixelized;

/**
 * Converts a Cfg to a CfgToPixelized using a transformer
 *
 * @author Owen Feehan
 */
public class PixelizeWithTransform<T> extends StateTransformerBean<T, ToPixelized<T>> {

    // START BEAN PROPERTIES
    @BeanField private StateTransformerBean<T, CfgNRGPixelized> transformer;
    // END BEAN PROPERTIES

    public PixelizeWithTransform() {}

    public PixelizeWithTransform(StateTransformerBean<T, CfgNRGPixelized> transformer) {
        super();
        this.transformer = transformer;
    }

    @Override
    public ToPixelized<T> transform(T in, TransformationContext context)
            throws OperationFailedException {
        return new ToPixelized<>(in, transformer.transform(in, context));
    }

    public StateTransformerBean<T, CfgNRGPixelized> getTransformer() {
        return transformer;
    }

    public void setTransformer(StateTransformerBean<T, CfgNRGPixelized> transformer) {
        this.transformer = transformer;
    }
}
