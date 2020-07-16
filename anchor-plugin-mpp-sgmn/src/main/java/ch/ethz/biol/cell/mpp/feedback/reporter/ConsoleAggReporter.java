/* (C)2020 */
package ch.ethz.biol.cell.mpp.feedback.reporter;

import lombok.NoArgsConstructor;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.feedback.ReporterAgg;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackEndParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackInitParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.ReporterException;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.aggregate.AggregateReceiver;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.aggregate.Aggregator;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.aggregate.AggregatorException;
import org.anchoranalysis.mpp.sgmn.optscheme.step.Reporting;
import org.apache.commons.lang.time.StopWatch;

@NoArgsConstructor
public final class ConsoleAggReporter extends ReporterAgg<CfgNRGPixelized>
        implements AggregateReceiver<CfgNRGPixelized> {

    private StopWatch timer = null;

    public ConsoleAggReporter(double aggIntervalLog10) {
        super(aggIntervalLog10);
    }

    @Override
    public void aggStart(OptimizationFeedbackInitParams<CfgNRGPixelized> initParams, Aggregator agg)
            throws AggregatorException {
        // NOTHING TO DO
    }

    @Override
    public void reportBegin(OptimizationFeedbackInitParams<CfgNRGPixelized> initParams)
            throws ReporterException {
        super.reportBegin(initParams);
        timer = new StopWatch();
        timer.start();
    }

    @Override
    public void aggReport(Reporting<CfgNRGPixelized> reporting, Aggregator agg) {
        System.out.printf( // NOSONAR
                "itr=%d  time=%e  tpi=%e   %s%n",
                reporting.getIter(),
                ((double) timer.getTime()) / 1000,
                ((double) timer.getTime()) / (reporting.getIter() * 1000),
                agg.toString());
    }

    @Override
    public void reportNewBest(Reporting<CfgNRGPixelized> reporting) throws ReporterException {
        System.out.printf( // NOSONAR
                "*** itr=%d  size=%d  best_nrg=%e  kernel=%s%n",
                reporting.getIter(),
                reporting.getCfgNRGAfter().getCfg().size(),
                reporting.getCfgNRGAfter().getCfgNRG().getNrgTotal(),
                reporting.getKernel().getDescription());
    }

    @Override
    public void aggEnd(Aggregator agg) {
        // NOTHING TO DO
    }

    @Override
    protected AggregateReceiver<CfgNRGPixelized> getAggregateReceiver() {
        return this;
    }

    @Override
    public void reportEnd(OptimizationFeedbackEndParams<CfgNRGPixelized> optStep) {
        super.reportEnd(optStep);
        timer.stop();
        System.out.printf(
                "Optimization time took %e s%n", ((double) timer.getTime()) / 1000); // NOSONAR
    }
}
