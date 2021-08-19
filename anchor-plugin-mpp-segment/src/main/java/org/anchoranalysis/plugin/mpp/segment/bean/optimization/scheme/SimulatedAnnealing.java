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

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.mpp.bean.AnnealScheme;
import org.anchoranalysis.mpp.feature.mark.UpdatableMarksList;
import org.anchoranalysis.mpp.segment.bean.optimization.OptimizationScheme;
import org.anchoranalysis.mpp.segment.bean.optimization.kernel.KernelProposer;
import org.anchoranalysis.mpp.segment.bean.optimization.termination.TerminationCondition;
import org.anchoranalysis.mpp.segment.bean.optimization.termination.TerminationConditionListOr;
import org.anchoranalysis.mpp.segment.optimization.OptimizationContext;
import org.anchoranalysis.mpp.segment.optimization.OptimizationTerminatedEarlyException;
import org.anchoranalysis.mpp.segment.optimization.feedback.FeedbackReceiver;
import org.anchoranalysis.mpp.segment.optimization.kernel.MarkFactoryContext;
import org.anchoranalysis.mpp.segment.transformer.TransformationContext;
import org.anchoranalysis.plugin.mpp.segment.bean.optimization.mode.AssignMode;

/**
 * Finds an optima using a simulated-annealing approach
 *
 * @author Owen Feehan
 * @param <S> state returned from algorithm, and reported to the outside world
 * @param <T> state used internally during optimization
 * @param <U> type of kernel proposer
 */
public class SimulatedAnnealing<S, T, U> extends OptimizationScheme<S, U, UpdatableMarksList> {

    // START BEAN PARAMETERS
    @BeanField @Getter @Setter private TerminationCondition termination;

    @BeanField @Getter @Setter private AnnealScheme annealScheme;

    @BeanField @Getter @Setter private AssignMode<S, T, U> assignMode;
    // END BEAN PARAMTERS

    @Override
    public String describeBean() {
        return String.format("%s", getBeanName());
    }

    // Finds an optimum by generating a certain number of configurations
    @Override
    public S findOptimum(
            KernelProposer<U, UpdatableMarksList> proposer,
            UpdatableMarksList marks,
            FeedbackReceiver<S> feedback,
            OptimizationContext initContext)
            throws OptimizationTerminatedEarlyException {

        MarkFactoryContext markFactoryContext = initContext.markFactoryContext();

        FeedbackGenerator<S> feedbackGenerator =
                FeedbackHelper.createInitFeedbackGenerator(
                        feedback,
                        initContext,
                        proposer.getAllKernelFactories(),
                        assignMode.extractScoreSizeReport());

        TransformationContext transformationContext =
                new TransformationContext(
                        initContext.getDualStack().getEnergyStack().dimensions(),
                        initContext.calculateContext(markFactoryContext),
                        initContext.getLogger());

        T best =
                SimulatedAnnealingHelper.doOptimization(
                        assignMode,
                        annealScheme,
                        marks,
                        feedbackGenerator,
                        proposer,
                        createTermCondition(initContext),
                        transformationContext);

        try {
            S bestTransformed =
                    assignMode
                            .stateReporter()
                            .primaryReport()
                            .transform(best, transformationContext);
            FeedbackHelper.endWithFinalFeedback(feedbackGenerator, bestTransformed, initContext);
            return bestTransformed;
        } catch (OperationFailedException e) {
            throw new OptimizationTerminatedEarlyException("Cannot do necessary transformation", e);
        }
    }

    private TerminationCondition createTermCondition(OptimizationContext initContext) {
        TerminationCondition termConditionAll =
                new TerminationConditionListOr(
                        termination, initContext.getTriggerTerminationCondition());
        termConditionAll.init();
        return termConditionAll;
    }
}
