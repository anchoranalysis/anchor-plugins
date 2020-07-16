/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.kernelbridge;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.mpp.sgmn.transformer.StateTransformer;
import org.anchoranalysis.mpp.sgmn.transformer.StateTransformerBean;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.cfg.pixelized.PixelizeWithTransform;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.cfg.pixelized.RetrieveSourceFromPixelized;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.mode.TransformMapOptional;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.optscheme.ToPixelized;

/**
 * State takes the form of ToPixelized<T> derived from the Kernel
 *
 * @author Owen Feehan
 * @param <U>
 * @param <T>
 */
public class KernelStateBridgePixelize<T> extends KernelStateBridge<T, ToPixelized<T>> {

    // START BEAN PROPERTIES
    @BeanField private StateTransformerBean<T, CfgNRGPixelized> transformer;
    // END BEAN PROPERTIES

    @Override
    public StateTransformer<Optional<T>, Optional<ToPixelized<T>>> kernelToState() {
        return new TransformMapOptional<>(new PixelizeWithTransform<>(transformer));
    }

    @Override
    public StateTransformer<Optional<ToPixelized<T>>, Optional<T>> stateToKernel() {
        return new TransformMapOptional<>(new RetrieveSourceFromPixelized<>());
    }

    public StateTransformerBean<T, CfgNRGPixelized> getTransformer() {
        return transformer;
    }

    public void setTransformer(StateTransformerBean<T, CfgNRGPixelized> transformer) {
        this.transformer = transformer;
    }
}
