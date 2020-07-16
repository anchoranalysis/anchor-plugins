/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.log.MessageLogger;
import org.anchoranalysis.mpp.sgmn.kernel.proposer.WeightedKernelList;
import org.anchoranalysis.mpp.sgmn.optscheme.ExtractScoreSize;
import org.anchoranalysis.mpp.sgmn.optscheme.OptSchemeContext;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.FeedbackReceiver;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackEndParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackInitParams;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class FeedbackHelper {

    public static <T> FeedbackGenerator<T> createInitFeedbackGenerator(
            FeedbackReceiver<T> feedbackReceiver,
            OptSchemeContext initContext,
            WeightedKernelList<?> allKernelFactories,
            ExtractScoreSize<T> extractScoreSize) {

        FeedbackGenerator<T> feedbackGenerator =
                new FeedbackGenerator<>(feedbackReceiver, initContext.getLogger().errorReporter());

        feedbackGenerator.begin(
                feedbackInitParams(initContext, allKernelFactories), extractScoreSize);

        return feedbackGenerator;
    }

    public static <T> void endWithFinalFeedback(
            FeedbackGenerator<T> feedbackGenerator, T state, OptSchemeContext initContext) {
        OptimizationFeedbackEndParams<T> optEndParams =
                feedbackEndParams(state, initContext.getLogger().messageLogger());
        feedbackGenerator.end(optEndParams);
    }

    private static <T> OptimizationFeedbackInitParams<T> feedbackInitParams(
            OptSchemeContext initContext, WeightedKernelList<?> allKernelFactories) {
        OptimizationFeedbackInitParams<T> feedbackParams = new OptimizationFeedbackInitParams<>();
        feedbackParams.setInitContext(initContext);
        feedbackParams.setKernelFactoryList(allKernelFactories);
        return feedbackParams;
    }

    private static <T> OptimizationFeedbackEndParams<T> feedbackEndParams(
            T state, MessageLogger logger) {
        OptimizationFeedbackEndParams<T> optEndParams = new OptimizationFeedbackEndParams<>();
        optEndParams.setState(state);
        optEndParams.setLogReporter(logger);
        return optEndParams;
    }
}
