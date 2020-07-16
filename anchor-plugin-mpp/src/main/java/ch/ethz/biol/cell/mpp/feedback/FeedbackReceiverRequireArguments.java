/* (C)2020 */
package ch.ethz.biol.cell.mpp.feedback;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.experiment.bean.require.RequireArguments;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.feedback.FeedbackReceiverBean;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackEndParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackInitParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.ReporterException;
import org.anchoranalysis.mpp.sgmn.optscheme.step.Reporting;

public class FeedbackReceiverRequireArguments<T> extends FeedbackReceiverBean<T> {

    // START BEAN PROPERTIES
    @BeanField @OptionalBean private FeedbackReceiverBean<T> feedbackReceiver;

    @BeanField private RequireArguments requireArguments;
    // END BEAN PROPERTIES

    private boolean doFeedback;

    @Override
    public void reportBegin(OptimizationFeedbackInitParams<T> optInit) throws ReporterException {

        doFeedback =
                requireArguments.hasAllRequiredArguments(optInit.getInitContext().isDebugEnabled());

        if (!doFeedback) {
            return;
        }

        feedbackReceiver.reportBegin(optInit);
    }

    @Override
    public void reportItr(Reporting<T> reporting) throws ReporterException {

        if (!doFeedback) {
            return;
        }

        feedbackReceiver.reportItr(reporting);
    }

    @Override
    public void reportNewBest(Reporting<T> reporting) throws ReporterException {

        if (!doFeedback) {
            return;
        }

        feedbackReceiver.reportNewBest(reporting);
    }

    @Override
    public void reportEnd(OptimizationFeedbackEndParams<T> optStep) throws ReporterException {

        if (!doFeedback) {
            return;
        }

        feedbackReceiver.reportEnd(optStep);
    }

    public FeedbackReceiverBean<T> getFeedbackReceiver() {
        return feedbackReceiver;
    }

    public void setFeedbackReceiver(FeedbackReceiverBean<T> feedbackReceiver) {
        this.feedbackReceiver = feedbackReceiver;
    }

    public RequireArguments getRequireArguments() {
        return requireArguments;
    }

    public void setRequireArguments(RequireArguments requireArguments) {
        this.requireArguments = requireArguments;
    }
}
