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
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.log.MessageLogger;
import org.anchoranalysis.mpp.bean.AnnealScheme;
import org.anchoranalysis.mpp.segment.bean.optimization.feedback.ExtractScoreSize;
import org.anchoranalysis.mpp.segment.bean.optimization.kernel.KernelProposer;
import org.anchoranalysis.mpp.segment.bean.optimization.termination.TerminationCondition;
import org.anchoranalysis.mpp.segment.optimization.OptimizationTerminatedEarlyException;
import org.anchoranalysis.mpp.segment.optimization.feedback.ReporterException;
import org.anchoranalysis.mpp.segment.optimization.step.OptimizationStep;
import org.anchoranalysis.mpp.segment.optimization.step.Reporting;
import org.anchoranalysis.mpp.segment.transformer.TransformationContext;
import org.anchoranalysis.plugin.mpp.segment.bean.optimization.mode.AssignMode;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class SimulatedAnnealingHelper {

    /**
     * Performs optimization
     *
     * @param assignMode
     * @param annealScheme
     * @param updatableState
     * @param feedbackGenerator
     * @param kernelProposer
     * @param termConditionAll
     * @param context
     * @param <S> type reported back
     * @param <T> optimization state
     * @param <U> kernel-proposer type
     * @param <V> updatable-state
     * @return
     * @throws OptimizationTerminatedEarlyException
     */
    public static <S, T, U, V> T doOptimization(
            AssignMode<S, T, U> assignMode,
            AnnealScheme annealScheme,
            V updatableState,
            FeedbackGenerator<S> feedbackGenerator,
            KernelProposer<U, V> kernelProposer,
            TerminationCondition termConditionAll,
            TransformationContext context)
            throws OptimizationTerminatedEarlyException {

        try {
            kernelProposer.initBeforeCalc(context.getKernelCalcContext());
        } catch (InitializeException e) {
            throw new OptimizationTerminatedEarlyException("Init failed", e);
        }

        OptimizationStep<U, T, V> step = new OptimizationStep<>();

        int iteration = 0;
        do {
            step.setTemperature(annealScheme.calculateTemperature(iteration));

            ProposalToConsider<U, T, V> proposal =
                    new ProposalToConsider<>(
                            step,
                            iteration,
                            assignMode.acceptableProbability(annealScheme),
                            context);
            proposal.applyKernelToStep(
                    kernelProposer,
                    assignMode.kernelAssigner(),
                    updatableState,
                    assignMode.kernelStateBridge().stateToKernel());

            try {
                reportOptimizationStep(
                        step.reporting(iteration, assignMode.stateReporter(), context),
                        feedbackGenerator);
            } catch (OperationFailedException | ReporterException e) {
                throw new OptimizationTerminatedEarlyException(
                        "Cannot create reporting for step", e);
            }
        } while (continueIterations(
                step.getBest(),
                ++iteration,
                termConditionAll,
                assignMode.extractScoreSizeState(),
                context.getLogger().messageLogger()));

        try {
            return step.releaseKeepBest();
        } catch (OperationFailedException e) {
            throw new OptimizationTerminatedEarlyException("Cannot release the best item", e);
        }
    }

    private static <T> boolean continueIterations(
            Optional<T> state,
            int iteration,
            TerminationCondition termConditionAll,
            ExtractScoreSize<T> extractScoreSize,
            MessageLogger logger) {
        if (state.isPresent()) {
            return termConditionAll.continueFurther(
                    iteration,
                    extractScoreSize.extractScore(state.get()),
                    extractScoreSize.extractSize(state.get()),
                    logger);
        } else {
            return true;
        }
    }

    private static <S> void reportOptimizationStep(
            Reporting<S> reporting, FeedbackGenerator<S> feedbackGenerator)
            throws ReporterException {

        if (reporting.isBest()) {
            feedbackGenerator.recordBest(reporting);
        }

        feedbackGenerator.trigger(reporting);
    }
}
