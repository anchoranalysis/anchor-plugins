/* (C)2020 */
package ch.ethz.biol.cell.mpp.feedback.reporter;

import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.feedback.ReporterAgg;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackInitParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.ReporterException;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.aggregate.AggregateReceiver;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.aggregate.Aggregator;
import org.anchoranalysis.mpp.sgmn.optscheme.step.Reporting;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Log4JReporter extends ReporterAgg<CfgNRGPixelized>
        implements AggregateReceiver<CfgNRGPixelized> {

    private static Log log = LogFactory.getLog(Log4JReporter.class);

    // Constructor
    public Log4JReporter() {
        super();
    }

    @Override
    protected AggregateReceiver<CfgNRGPixelized> getAggregateReceiver() {
        return this;
    }

    @Override
    public void reportNewBest(Reporting<CfgNRGPixelized> reporting) throws ReporterException {

        String out =
                String.format(
                        "*** itr=%d  size=%d  best_nrg=%e  kernel=%s",
                        reporting.getIter(),
                        reporting.getCfgNRGAfter().getCfg().size(),
                        reporting.getCfgNRGAfter().getCfgNRG().getNrgTotal(),
                        reporting.getKernel().getDescription());
        log.info(out);
    }

    @Override
    public void aggStart(
            OptimizationFeedbackInitParams<CfgNRGPixelized> initParams, Aggregator agg) {
        // NOTHING TO DO
    }

    @Override
    public void aggReport(Reporting<CfgNRGPixelized> reporting, Aggregator agg) {
        log.info(String.format("itr=%d  %s", reporting.getIter(), agg.toString()));
    }

    @Override
    public void aggEnd(Aggregator agg) {
        // NOTHING TO DO
    }
}
