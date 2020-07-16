/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.kernel.updater;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.mark.set.UpdateMarkSetException;
import org.anchoranalysis.mpp.sgmn.bean.kernel.Kernel;
import org.anchoranalysis.mpp.sgmn.transformer.TransformationContext;

/**
 * @author Owen Feehan
 * @param <S> kernel type
 * @param <T> state-type
 */
public interface KernelUpdater<S, T> {

    /**
     * Informs that a proposal from a particular kernel has been accepted
     *
     * @param kernel the kernel whose proposal was accepted
     * @param crnt the existing state
     * @param proposed the new state that was accepted
     * @param context
     * @throws UpdateMarkSetException
     */
    void kernelAccepted(
            Kernel<S> kernel, Optional<T> crnt, T proposed, TransformationContext context)
            throws UpdateMarkSetException;
}
