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

package org.anchoranalysis.plugin.mpp.segment.bean.optimization.scheme;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.MessageLogger;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.mpp.bean.anneal.AnnealScheme;
import org.anchoranalysis.mpp.feature.mark.ListUpdatableMarkSetCollection;
import org.anchoranalysis.mpp.mark.set.UpdateMarkSetException;
import org.anchoranalysis.mpp.proposer.error.ErrorNode;
import org.anchoranalysis.mpp.proposer.error.ProposerFailureDescription;
import org.anchoranalysis.mpp.segment.bean.kernel.proposer.KernelProposer;
import org.anchoranalysis.mpp.segment.bean.optimization.ExtractScoreSize;
import org.anchoranalysis.mpp.segment.bean.optimization.termination.TerminationCondition;
import org.anchoranalysis.mpp.segment.kernel.KernelAssigner;
import org.anchoranalysis.mpp.segment.kernel.KernelCalculateEnergyException;
import org.anchoranalysis.mpp.segment.kernel.proposer.KernelWithIdentifier;
import org.anchoranalysis.mpp.segment.optimization.OptimizationTerminatedEarlyException;
import org.anchoranalysis.mpp.segment.optimization.feedback.ReporterException;
import org.anchoranalysis.mpp.segment.optimization.step.OptimizationStep;
import org.anchoranalysis.mpp.segment.optimization.step.Reporting;
import org.anchoranalysis.mpp.segment.transformer.StateTransformer;
import org.anchoranalysis.mpp.segment.transformer.TransformationContext;
import org.anchoranalysis.plugin.mpp.segment.bean.optimization.mode.AssignMode;
import org.anchoranalysis.plugin.mpp.segment.kernel.assigner.KernelAssignerAddErrorLevel;
import org.anchoranalysis.plugin.mpp.segment.kernel.updater.KernelUpdater;
import org.anchoranalysis.plugin.mpp.segment.kernel.updater.KernelUpdaterSimple;
import org.anchoranalysis.plugin.mpp.segment.optimization.AccptProbCalculator;
import org.apache.commons.lang.time.StopWatch;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class SimulatedAnnealingHelper {

    /**
     * Performs optimization
     *
     * @param assignMode
     * @param annealScheme
     * @param updatableMarkSetCollection
     * @param feedbackGenerator
     * @param kernelProposer
     * @param termConditionAll
     * @param context
     * @param <S> type reported back
     * @param <T> optimization state
     * @param <U> kernel-proposer type
     * @return
     * @throws OptimizationTerminatedEarlyException
     */
    public static <S, T, U> T doOptimization(
            AssignMode<S, T, U> assignMode,
            AnnealScheme annealScheme,
            ListUpdatableMarkSetCollection updatableMarkSetCollection,
            FeedbackGenerator<S> feedbackGenerator,
            KernelProposer<U> kernelProposer,
            TerminationCondition termConditionAll,
            TransformationContext context)
            throws OptimizationTerminatedEarlyException {

        try {
            kernelProposer.initBeforeCalc(context.getKernelCalcContext());
        } catch (InitException e) {
            throw new OptimizationTerminatedEarlyException("Init failed", e);
        }

        OptimizationStep<U, T> optStep = new OptimizationStep<>();

        int iter = 0;
        do {
            optStep.setTemperature(annealScheme.calculateTemperature(iter));

            applyKernelToOptStep(
                    optStep,
                    iter,
                    context,
                    kernelProposer,
                    updatableMarkSetCollection,
                    assignMode.probCalculator(annealScheme),
                    assignMode.kernelAssigner(),
                    assignMode.kernelStateBridge().stateToKernel());

            try {
                reportOptStep(
                        optStep.reporting(iter, assignMode.stateReporter(), context),
                        feedbackGenerator);
            } catch (OperationFailedException | ReporterException e) {
                throw new OptimizationTerminatedEarlyException("Cannot create reporting for optStep", e);
            }
        } while (continueIterations(
                optStep.getBest(),
                ++iter,
                termConditionAll,
                assignMode.extractScoreSizeState(),
                context.getLogger().messageLogger()));

        try {
            return optStep.releaseKeepBest();
        } catch (OperationFailedException e) {
            throw new OptimizationTerminatedEarlyException("Cannot release the best item", e);
        }
    }

    private static <T> boolean continueIterations(
            Optional<T> state,
            int iter,
            TerminationCondition termConditionAll,
            ExtractScoreSize<T> extractScoreSize,
            MessageLogger logger) {
        if (!state.isPresent()) {
            return true;
        }

        return termConditionAll.continueIterations(
                iter,
                extractScoreSize.extractScore(state.get()),
                extractScoreSize.extractSize(state.get()),
                logger);
    }

    private static <S> void reportOptStep(
            Reporting<S> reporting, FeedbackGenerator<S> feedbackGenerator)
            throws ReporterException {

        if (reporting.isBest()) {
            feedbackGenerator.recordBest(reporting);
        }

        feedbackGenerator.record(reporting);
    }

    private static <S, T> void applyKernelToOptStep(
            OptimizationStep<S, T> optStep,
            int iter,
            TransformationContext context,
            KernelProposer<S> kernelProposer,
            ListUpdatableMarkSetCollection updatableMarkSetCollection,
            AccptProbCalculator<T> accptProbCalc,
            KernelAssigner<S, T> kernelAssigner,
            StateTransformer<Optional<T>, Optional<S>> funcExtractForUpdate)
            throws OptimizationTerminatedEarlyException {
        try {
            // Propose a kernel
            KernelWithIdentifier<S> kid =
                    proposeKernel(
                            kernelProposer,
                            context.getKernelCalcContext().proposer().getRandomNumberGenerator(),
                            iter == 0);

            KernelUpdater<S, T> kernelUpdater =
                    new KernelUpdaterSimple<>(
                            updatableMarkSetCollection,
                            kernelProposer.getAllKernelFactories(),
                            funcExtractForUpdate);

            assignToOptStepForKernel(
                    optStep, iter, kid, context, accptProbCalc, kernelUpdater, kernelAssigner);

        } catch (KernelCalculateEnergyException e) {
            throw new OptimizationTerminatedEarlyException("A kernel-calculation error occurred", e);
        } catch (UpdateMarkSetException e) {
            throw new OptimizationTerminatedEarlyException("An update-mask-set error occurred", e);
        }
    }

    private static <S> KernelWithIdentifier<S> proposeKernel(
            KernelProposer<S> kernelProposer,
            RandomNumberGenerator randomNumberGenerator,
            boolean firstStep) {
        if (firstStep) {
            return kernelProposer.initialKernel();
        } else {
            return kernelProposer.proposeKernel(randomNumberGenerator);
        }
    }

    private static <S, T> void assignToOptStepForKernel(
            OptimizationStep<S, T> optStep,
            int iter,
            KernelWithIdentifier<S> kid,
            TransformationContext context,
            AccptProbCalculator<T> accptProbCalc,
            KernelUpdater<S, T> kernelUpdater,
            KernelAssigner<S, T> kernelAssigner)
            throws KernelCalculateEnergyException, UpdateMarkSetException {

        StopWatch timer = new StopWatch();
        timer.start();

        // We switch off debugging for now to check out the synchronization problems
        ProposerFailureDescription error = new ProposerFailureDescription();

        // We assign the proposed marks
        createAssigner(kernelAssigner, error.getRoot()).assignProposal(optStep, context, kid);

        ConsiderProposalHelper.maybeAcceptProposal(
                optStep, iter, accptProbCalc, kernelUpdater, error, context);

        assignExecutionTime(optStep, timer);
    }

    private static <S, T> KernelAssigner<S, T> createAssigner(
            KernelAssigner<S, T> kernelAssigner, ErrorNode error) {
        return new KernelAssignerAddErrorLevel<>(kernelAssigner, error);
    }

    private static void assignExecutionTime(OptimizationStep<?, ?> optStep, StopWatch timer) {
        long time = timer.getTime();
        timer.reset();
        optStep.setExecutionTime(time);
    }
}
