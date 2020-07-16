/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.anchor.mpp.mark.set.UpdateMarkSetException;
import org.anchoranalysis.anchor.mpp.proposer.error.ProposerFailureDescription;
import org.anchoranalysis.mpp.sgmn.optscheme.step.OptimizationStep;
import org.anchoranalysis.mpp.sgmn.transformer.TransformationContext;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.kernel.updater.KernelUpdater;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.optscheme.AccptProbCalculator;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ConsiderProposalHelper {

    public static <S, T> void maybeAcceptProposal(
            OptimizationStep<S, T> optStep,
            int iter,
            AccptProbCalculator<T> accptProbCalculator,
            KernelUpdater<S, T> kernelUpdater,
            ProposerFailureDescription error,
            TransformationContext context)
            throws UpdateMarkSetException {

        // If the kernel could not propose a new CfgNRG, then we treat this step as rejected
        if (optStep.getProposal().isPresent()) {
            considerProposal(
                    optStep,
                    optStep.getProposal().get(),
                    iter,
                    accptProbCalculator,
                    kernelUpdater,
                    context);
        } else {
            optStep.markNoProposal(error);
        }
    }

    private static <S, T> void considerProposal(
            OptimizationStep<S, T> optStep,
            T proposal,
            int iter,
            AccptProbCalculator<T> accptProbCalculator,
            KernelUpdater<S, T> kernelUpdater,
            TransformationContext context)
            throws UpdateMarkSetException {

        Optional<T> crnt = optStep.getCrnt();

        double accptProb =
                accptProbCalculator.calcAccptProb(
                        optStep.getKernel().getKernel(),
                        crnt,
                        proposal,
                        iter,
                        context.getKernelCalcContext());
        double randomValueBetweenZeroAndOne =
                context.getKernelCalcContext()
                        .proposer()
                        .getRandomNumberGenerator()
                        .sampleDoubleZeroAndOne();

        // check that the proposal actually contains a change
        assert !Double.isNaN(accptProb);

        if (randomValueBetweenZeroAndOne <= accptProb || !optStep.getBest().isPresent()) {

            if (crnt.isPresent()) {
                assert (!crnt.get().equals(proposal));
            }

            // We inform the kernels that we've accepted a CfgNRG
            kernelUpdater.kernelAccepted(optStep.getKernel().getKernel(), crnt, proposal, context);

            optStep.acceptProposal(accptProbCalculator.getFuncScore());

        } else {
            optStep.rejectProposal();
        }
    }
}
