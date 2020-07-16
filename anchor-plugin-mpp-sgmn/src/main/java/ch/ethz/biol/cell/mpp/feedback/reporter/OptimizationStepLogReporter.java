/* (C)2020 */
package ch.ethz.biol.cell.mpp.feedback.reporter;

import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.core.log.MessageLogger;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.feedback.ReporterAgg;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackEndParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackInitParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.ReporterException;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.aggregate.AggregateReceiver;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.aggregate.Aggregator;
import org.anchoranalysis.mpp.sgmn.optscheme.step.Reporting;
import org.apache.commons.lang.time.StopWatch;

public final class OptimizationStepLogReporter extends ReporterAgg<CfgNRGPixelized>
        implements AggregateReceiver<CfgNRGPixelized> {

    private StopWatch timer;
    private MessageLogger logger;

    @Override
    public void reportBegin(OptimizationFeedbackInitParams<CfgNRGPixelized> initParams)
            throws ReporterException {

        super.reportBegin(initParams);

        timer = new StopWatch();
        timer.start();

        logger = initParams.getInitContext().getLogger().messageLogger();
    }

    @Override
    public void aggStart(
            OptimizationFeedbackInitParams<CfgNRGPixelized> initParams, Aggregator agg) {
        // NOTHING TO DO
    }

    @Override
    public void reportNewBest(Reporting<CfgNRGPixelized> reporting) throws ReporterException {

        logger.logFormatted(
                "*** itr=%d  size=%d  best_nrg=%e  kernel=%s",
                reporting.getIter(),
                reporting.getCfgNRGAfter().getCfg().size(),
                reporting.getCfgNRGAfter().getCfgNRG().getNrgTotal(),
                reporting.getKernel().getDescription());
    }

    @Override
    public void aggReport(Reporting<CfgNRGPixelized> reporting, Aggregator agg) {

        logger.logFormatted(
                "itr=%d  time=%e  tpi=%e  %s",
                reporting.getIter(),
                ((double) timer.getTime()) / 1000,
                ((double) timer.getTime()) / (reporting.getIter() * 1000),
                agg.toString());
    }

    @Override
    public void aggEnd(Aggregator agg) {
        // NOTHING TO DO
    }

    @Override
    public void reportEnd(OptimizationFeedbackEndParams<CfgNRGPixelized> optStep) {

        timer.stop();

        optStep.getLogReporter().log(optStep.getState().toString());
        optStep.getLogReporter()
                .logFormatted("Optimization time took %e s%n", ((double) timer.getTime()) / 1000);

        super.reportEnd(optStep);
    }

    @Override
    protected AggregateReceiver<CfgNRGPixelized> getAggregateReceiver() {
        return this;
    }
}
