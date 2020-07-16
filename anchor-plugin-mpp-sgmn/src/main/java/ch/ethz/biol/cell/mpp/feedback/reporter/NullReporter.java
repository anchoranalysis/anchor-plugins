/* (C)2020 */
package ch.ethz.biol.cell.mpp.feedback.reporter;

import org.anchoranalysis.mpp.sgmn.bean.optscheme.feedback.FeedbackReceiverBean;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackEndParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackInitParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.ReporterException;
import org.anchoranalysis.mpp.sgmn.optscheme.step.Reporting;

/**
 * Dummy reporter that takes no action.
 *
 * @author Owen Feehan
 * @param <T> reporter-type
 */
public class NullReporter<T> extends FeedbackReceiverBean<T> {

    @Override
    public void reportBegin(OptimizationFeedbackInitParams<T> optInit) throws ReporterException {
        // NOTHING TO DO
    }

    @Override
    public void reportItr(Reporting<T> reporting) {
        // NOTHING TO DO
    }

    @Override
    public void reportNewBest(Reporting<T> reporting) {
        // NOTHING TO DO
    }

    @Override
    public void reportEnd(OptimizationFeedbackEndParams<T> optStep) {
        // NOTHING TO DO
    }
}
