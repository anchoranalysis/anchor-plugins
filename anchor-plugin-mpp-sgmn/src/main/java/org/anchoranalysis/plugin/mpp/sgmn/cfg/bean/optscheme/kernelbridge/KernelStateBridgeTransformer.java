/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.kernelbridge;

import java.util.Optional;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.mpp.sgmn.transformer.StateTransformer;
import org.anchoranalysis.mpp.sgmn.transformer.StateTransformerBean;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.mode.TransformMapOptional;

public class KernelStateBridgeTransformer<U, T> extends KernelStateBridge<U, T> {

    // START BEAN PROPERTIES
    @BeanField private StateTransformerBean<U, T> transformerKernelToState;

    @BeanField private StateTransformerBean<T, U> transformerStateToKernel;
    // END BEAN PROPERTIES

    @Override
    public StateTransformer<Optional<U>, Optional<T>> kernelToState() {
        return new TransformMapOptional<>(transformerKernelToState);
    }

    @Override
    public StateTransformer<Optional<T>, Optional<U>> stateToKernel() {
        return new TransformMapOptional<>(transformerStateToKernel);
    }

    public StateTransformerBean<U, T> getTransformerKernelToState() {
        return transformerKernelToState;
    }

    public void setTransformerKernelToState(StateTransformerBean<U, T> transformerKernelToState) {
        this.transformerKernelToState = transformerKernelToState;
    }

    public StateTransformerBean<T, U> getTransformerStateToKernel() {
        return transformerStateToKernel;
    }

    public void setTransformerStateToKernel(StateTransformerBean<T, U> transformerStateToKernel) {
        this.transformerStateToKernel = transformerStateToKernel;
    }
}
