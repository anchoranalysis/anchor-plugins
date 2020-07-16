/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.kernelbridge;

import java.util.Optional;
import org.anchoranalysis.mpp.sgmn.transformer.StateTransformer;

/**
 * No transformation occurs as Kernel and State are the same type
 *
 * @author Owen Feehan
 * @param <T>
 */
public class KernelStateBridgeIdentity<T> extends KernelStateBridge<T, T> {

    @Override
    public StateTransformer<Optional<T>, Optional<T>> kernelToState() {
        return stateToKernel();
    }

    @Override
    public StateTransformer<Optional<T>, Optional<T>> stateToKernel() {
        return (a, context) -> a;
    }
}
