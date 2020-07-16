/*-
 * #%L
 * anchor-plugin-mpp-sgmn
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
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
