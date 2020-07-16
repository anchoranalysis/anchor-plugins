/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.kernel.assigner;

import java.util.Optional;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.mpp.sgmn.kernel.KernelAssigner;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcNRGException;
import org.anchoranalysis.mpp.sgmn.kernel.proposer.KernelWithID;
import org.anchoranalysis.mpp.sgmn.optscheme.step.OptimizationStep;
import org.anchoranalysis.mpp.sgmn.transformer.TransformationContext;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.kernelbridge.KernelStateBridge;

/**
 * @author Owen Feehan
 * @param <S> Optimization state
 * @param <T> Knerel type
 */
public class KernelAssignerCalcNRGFromKernel<S, T> implements KernelAssigner<S, T> {

    private KernelStateBridge<S, T> kernelStateBridge;

    public KernelAssignerCalcNRGFromKernel(KernelStateBridge<S, T> kernelStateBridge) {
        super();
        this.kernelStateBridge = kernelStateBridge;
    }

    @Override
    public void assignProposal(
            OptimizationStep<S, T> optStep, TransformationContext context, KernelWithID<S> kid)
            throws KernelCalcNRGException {

        try {
            Optional<S> prop =
                    kid.getKernel()
                            .makeProposal(
                                    kernelStateBridge
                                            .stateToKernel()
                                            .transform(optStep.getCrnt(), context),
                                    context.getKernelCalcContext());

            optStep.assignProposal(
                    OptionalUtilities.flatMap(
                            prop,
                            proposal -> kernelStateBridge.kernelToState().transform(prop, context)),
                    kid);

        } catch (OperationFailedException e) {
            throw new KernelCalcNRGException("Cannot transform function", e);
        }
    }
}
