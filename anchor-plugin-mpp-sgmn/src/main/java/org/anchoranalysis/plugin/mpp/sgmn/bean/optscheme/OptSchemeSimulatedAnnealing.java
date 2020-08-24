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

package org.anchoranalysis.plugin.mpp.sgmn.bean.optscheme;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.anneal.AnnealScheme;
import org.anchoranalysis.anchor.mpp.feature.mark.ListUpdatableMarkSetCollection;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.mpp.sgmn.bean.kernel.proposer.KernelProposer;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.OptScheme;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.termination.TerminationCondition;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.termination.TerminationConditionListOr;
import org.anchoranalysis.mpp.sgmn.kernel.MarkFactoryContext;
import org.anchoranalysis.mpp.sgmn.optscheme.OptSchemeContext;
import org.anchoranalysis.mpp.sgmn.optscheme.OptTerminatedEarlyException;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.FeedbackReceiver;
import org.anchoranalysis.mpp.sgmn.transformer.TransformationContext;
import org.anchoranalysis.plugin.mpp.sgmn.bean.optscheme.mode.AssignMode;

/**
 * Finds an optima using a simulated-annealing approach
 *
 * @author Owen Feehan
 * @param <S> state returned from algorithm, and reported to the outside world
 * @param <T> state used internally during optimization
 * @param <U> type of kernel proposer
 */
public class OptSchemeSimulatedAnnealing<S, T, U> extends OptScheme<S, U> {

    // START BEAN PARAMETERS
    @BeanField @Getter @Setter private TerminationCondition termCondition = null;

    @BeanField @Getter @Setter private AnnealScheme annealScheme = null;

    @BeanField @Getter @Setter private AssignMode<S, T, U> assignMode;
    // END BEAN PARAMTERS

    @Override
    public String describeBean() {
        return String.format("%s", getBeanName());
    }

    // Finds an optimum by generating a certain number of configurations
    @Override
    public S findOpt(
            KernelProposer<U> kernelProposer,
            ListUpdatableMarkSetCollection updatableMarkSetCollection,
            FeedbackReceiver<S> feedbackReceiver,
            OptSchemeContext initContext)
            throws OptTerminatedEarlyException {

        MarkFactoryContext markFactoryContext = initContext.markFactoryContext();

        FeedbackGenerator<S> feedbackGenerator =
                FeedbackHelper.createInitFeedbackGenerator(
                        feedbackReceiver,
                        initContext,
                        kernelProposer.getAllKernelFactories(),
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
                        updatableMarkSetCollection,
                        feedbackGenerator,
                        kernelProposer,
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
            throw new OptTerminatedEarlyException("Cannot do necessary transformation", e);
        }
    }

    private TerminationCondition createTermCondition(OptSchemeContext initContext) {
        TerminationCondition termConditionAll =
                new TerminationConditionListOr(
                        termCondition, initContext.getTriggerTerminationCondition());
        termConditionAll.init();
        return termConditionAll;
    }
}
