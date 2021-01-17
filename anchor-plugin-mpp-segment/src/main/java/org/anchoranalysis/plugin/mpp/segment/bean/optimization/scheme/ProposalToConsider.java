/*-
 * #%L
 * anchor-plugin-mpp-segment
 * %%
 * Copyright (C) 2010 - 2021 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
package org.anchoranalysis.plugin.mpp.segment.bean.optimization.scheme;

import java.util.Optional;
import lombok.AllArgsConstructor;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.mpp.mark.set.UpdateMarkSetException;
import org.anchoranalysis.mpp.proposer.error.ErrorNode;
import org.anchoranalysis.mpp.proposer.error.ProposerFailureDescription;
import org.anchoranalysis.mpp.segment.bean.kernel.proposer.KernelProposer;
import org.anchoranalysis.mpp.segment.kernel.KernelAssigner;
import org.anchoranalysis.mpp.segment.kernel.KernelCalculateEnergyException;
import org.anchoranalysis.mpp.segment.kernel.proposer.KernelWithIdentifier;
import org.anchoranalysis.mpp.segment.optimization.OptimizationTerminatedEarlyException;
import org.anchoranalysis.mpp.segment.optimization.step.OptimizationStep;
import org.anchoranalysis.mpp.segment.transformer.StateTransformer;
import org.anchoranalysis.mpp.segment.transformer.TransformationContext;
import org.anchoranalysis.plugin.mpp.segment.kernel.assigner.KernelAssignerAddErrorLevel;
import org.anchoranalysis.plugin.mpp.segment.kernel.updater.KernelUpdater;
import org.anchoranalysis.plugin.mpp.segment.kernel.updater.KernelUpdaterSimple;
import org.anchoranalysis.plugin.mpp.segment.optimization.AcceptanceProbabilityCalculator;
import org.apache.commons.lang.time.StopWatch;

/**
 * A proposal that may be accepted or rejected.
 *
 * @author Owen Feehan
 * @param <S> kernel-type
 * @param <T> optimization-state type
 * @param <V> updatable-state type
 */
@AllArgsConstructor
class ProposalToConsider<S, T, V> {

    private final OptimizationStep<S, T, V> step;
    private final int iteration;
    private final AcceptanceProbabilityCalculator<T> acceptanceProbability;
    private final TransformationContext context;

    public void applyKernelToStep(
            KernelProposer<S, V> kernelProposer,
            KernelAssigner<S, T, V> kernelAssigner,
            V updatableState,
            StateTransformer<Optional<T>, Optional<S>> funcExtractForUpdate)
            throws OptimizationTerminatedEarlyException {
        try {
            // Propose a kernel
            KernelWithIdentifier<S, V> kid =
                    proposeKernel(
                            kernelProposer,
                            context.getKernelCalcContext().proposer().getRandomNumberGenerator(),
                            iteration == 0);

            KernelUpdater<S, T, V> kernelUpdater =
                    new KernelUpdaterSimple<>(
                            updatableState,
                            kernelProposer.getAllKernelFactories(),
                            funcExtractForUpdate);

            assignToStepForKernel(kid, kernelAssigner, kernelUpdater);

        } catch (KernelCalculateEnergyException e) {
            throw new OptimizationTerminatedEarlyException(
                    "A kernel-calculation error occurred", e);
        } catch (UpdateMarkSetException e) {
            throw new OptimizationTerminatedEarlyException("An update-mask-set error occurred", e);
        }
    }

    private void assignToStepForKernel(
            KernelWithIdentifier<S, V> kid,
            KernelAssigner<S, T, V> kernelAssigner,
            KernelUpdater<S, T, V> kernelUpdater)
            throws KernelCalculateEnergyException, UpdateMarkSetException {

        StopWatch timer = new StopWatch();
        timer.start();

        // We switch off debugging for now to check out the synchronization problems
        ProposerFailureDescription error = new ProposerFailureDescription();

        // We assign the proposed marks
        createAssigner(kernelAssigner, error.getRoot()).assignProposal(step, context, kid);

        maybeAcceptProposal(error, kernelUpdater);
        assignExecutionTime(step, timer);
    }

    private void maybeAcceptProposal(
            ProposerFailureDescription error, KernelUpdater<S, T, V> kernelUpdater)
            throws UpdateMarkSetException {

        // If the kernel could not propose new marks, then we treat this step as rejected
        if (step.getProposal().isPresent()) {
            considerProposal(step.getProposal().get(), kernelUpdater); // NOSONAR
        } else {
            step.markNoProposal(error);
        }
    }

    private void considerProposal(T proposal, KernelUpdater<S, T, V> kernelUpdater)
            throws UpdateMarkSetException {

        Optional<T> current = step.getCurrent();

        double accptanceProbability =
                acceptanceProbability.calculateAcceptanceProbability(
                        step.getKernel().getKernel(),
                        current,
                        proposal,
                        iteration,
                        context.getKernelCalcContext());
        double randomValueBetweenZeroAndOne =
                context.getKernelCalcContext()
                        .proposer()
                        .getRandomNumberGenerator()
                        .sampleDoubleZeroAndOne();

        // check that the proposal actually contains a change
        assert !Double.isNaN(accptanceProbability);

        if (randomValueBetweenZeroAndOne <= accptanceProbability || !step.getBest().isPresent()) {

            // We inform the kernels that we've accepted new marks
            kernelUpdater.kernelAccepted(step.getKernel().getKernel(), current, proposal, context);

            step.acceptProposal(acceptanceProbability.getScoreExtracter());

        } else {
            step.rejectProposal();
        }
    }

    private static <S, T, U> KernelAssigner<S, T, U> createAssigner(
            KernelAssigner<S, T, U> kernelAssigner, ErrorNode error) {
        return new KernelAssignerAddErrorLevel<>(kernelAssigner, error);
    }

    private static void assignExecutionTime(OptimizationStep<?, ?, ?> step, StopWatch timer) {
        long time = timer.getTime();
        timer.reset();
        step.setExecutionTime(time);
    }

    private static <S, U> KernelWithIdentifier<S, U> proposeKernel(
            KernelProposer<S, U> kernelProposer,
            RandomNumberGenerator randomNumberGenerator,
            boolean firstStep) {
        if (firstStep) {
            return kernelProposer.initialKernel();
        } else {
            return kernelProposer.proposeKernel(randomNumberGenerator);
        }
    }
}
