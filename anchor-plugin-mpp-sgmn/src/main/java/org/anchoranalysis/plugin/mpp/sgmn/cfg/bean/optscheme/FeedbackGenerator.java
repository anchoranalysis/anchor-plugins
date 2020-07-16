/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.mpp.sgmn.optscheme.ExtractScoreSize;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.FeedbackReceiver;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackEndParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackInitParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.ReporterException;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.aggregate.AggregateTriggerBank;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.aggregate.AggregatorException;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.period.PeriodTriggerBank;
import org.anchoranalysis.mpp.sgmn.optscheme.step.Reporting;

// NB we execute every feedback action in a separate thread
//  on the Swing's event dispatch thread
class FeedbackGenerator<T> {

    private PeriodTriggerBank<T> periodTriggerBank;
    private AggregateTriggerBank<T> aggregateTriggerBank;
    private FeedbackReceiver<T> feedbackReceiver;
    private ErrorReporter errorReporter;

    public FeedbackGenerator(FeedbackReceiver<T> feedbackReceiver, ErrorReporter errorReporter) {
        super();
        this.feedbackReceiver = feedbackReceiver;
        this.errorReporter = errorReporter;
    }

    public void begin(OptimizationFeedbackInitParams<T> initParams, ExtractScoreSize<T> extracter) {

        this.periodTriggerBank = new PeriodTriggerBank<>();
        this.aggregateTriggerBank = new AggregateTriggerBank<>(extracter);

        initParams.setPeriodTriggerBank(periodTriggerBank);
        initParams.setAggregateTriggerBank(aggregateTriggerBank);

        try {
            feedbackReceiver.reportBegin(initParams);

            aggregateTriggerBank.start(initParams);
        } catch (AggregatorException | ReporterException e) {
            errorReporter.recordError(FeedbackGenerator.class, e);
        }
        periodTriggerBank.reset();
    }

    public void recordBest(Reporting<T> reporting) {

        try {
            feedbackReceiver.reportNewBest(reporting);
        } catch (ReporterException e) {
            errorReporter.recordError(FeedbackGenerator.class, e);
        }
    }

    public void record(Reporting<T> reporting) throws ReporterException {

        aggregateTriggerBank.record(reporting);

        try {
            periodTriggerBank.incr(reporting);
        } catch (OperationFailedException e) {
            errorReporter.recordError(FeedbackGenerator.class, e);
        }

        try {
            feedbackReceiver.reportItr(reporting);
        } catch (ReporterException e) {
            errorReporter.recordError(FeedbackGenerator.class, e);
        }
    }

    public void end(OptimizationFeedbackEndParams<T> optStep) {

        try {
            aggregateTriggerBank.end();
        } catch (AggregatorException e) {
            errorReporter.recordError(FeedbackGenerator.class, e);
        }
        try {
            feedbackReceiver.reportEnd(optStep);
        } catch (ReporterException e) {
            errorReporter.recordError(FeedbackGenerator.class, e);
        }
    }
}
