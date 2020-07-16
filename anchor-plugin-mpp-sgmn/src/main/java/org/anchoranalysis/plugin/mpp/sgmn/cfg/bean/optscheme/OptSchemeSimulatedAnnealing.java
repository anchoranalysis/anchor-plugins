/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme;

import org.anchoranalysis.anchor.mpp.bean.anneal.AnnealScheme;
import org.anchoranalysis.anchor.mpp.feature.mark.ListUpdatableMarkSetCollection;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.mpp.sgmn.bean.kernel.proposer.KernelProposer;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.OptScheme;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.termination.TerminationCondition;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.termination.TerminationConditionListOr;
import org.anchoranalysis.mpp.sgmn.kernel.CfgGenContext;
import org.anchoranalysis.mpp.sgmn.optscheme.OptSchemeContext;
import org.anchoranalysis.mpp.sgmn.optscheme.OptTerminatedEarlyException;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.FeedbackReceiver;
import org.anchoranalysis.mpp.sgmn.transformer.TransformationContext;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.mode.AssignMode;

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
    @BeanField private TerminationCondition termCondition = null;

    @BeanField private AnnealScheme annealScheme = null;

    @BeanField private AssignMode<S, T, U> assignMode;
    // END BEAN PARAMTERS

    @Override
    public String getBeanDscr() {
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

        CfgGenContext cfgGenContext = initContext.cfgGenContext();

        FeedbackGenerator<S> feedbackGenerator =
                FeedbackHelper.createInitFeedbackGenerator(
                        feedbackReceiver,
                        initContext,
                        kernelProposer.getAllKernelFactories(),
                        assignMode.extractScoreSizeReport());

        TransformationContext transformationContext =
                new TransformationContext(
                        initContext.getDualStack().getNrgStack().getDimensions(),
                        initContext.calcContext(cfgGenContext),
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

    public TerminationCondition getTermCondition() {
        return termCondition;
    }

    public void setTermCondition(TerminationCondition termCondition) {
        this.termCondition = termCondition;
    }

    public AnnealScheme getAnnealScheme() {
        return annealScheme;
    }

    public void setAnnealScheme(AnnealScheme annealScheme) {
        this.annealScheme = annealScheme;
    }

    public AssignMode<S, T, U> getAssignMode() {
        return assignMode;
    }

    public void setAssignMode(AssignMode<S, T, U> assignMode) {
        this.assignMode = assignMode;
    }
}
