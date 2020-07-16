/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.kernelbridge;

import java.util.Optional;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.mpp.sgmn.transformer.StateTransformer;

/**
 * Transforms a kernel-to-state and vice versa
 *
 * @author Owen Feehan
 * @param <U> kernel-type
 * @param <T> state-type
 */
public abstract class KernelStateBridge<U, T> extends AnchorBean<KernelStateBridge<U, T>> {

    public abstract StateTransformer<Optional<U>, Optional<T>> kernelToState();

    public abstract StateTransformer<Optional<T>, Optional<U>> stateToKernel();
}
