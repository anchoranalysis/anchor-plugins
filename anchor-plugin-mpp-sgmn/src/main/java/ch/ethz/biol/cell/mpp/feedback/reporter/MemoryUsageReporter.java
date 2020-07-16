/* (C)2020 */
package ch.ethz.biol.cell.mpp.feedback.reporter;

import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.log.MessageLogger;
import org.anchoranalysis.core.memory.MemoryUtilities;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.feedback.ReporterAgg;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackInitParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.aggregate.AggregateReceiver;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.aggregate.Aggregator;
import org.anchoranalysis.mpp.sgmn.optscheme.step.Reporting;

public class MemoryUsageReporter extends ReporterAgg<CfgNRGPixelized> {

    // START BEAN PROPERTIES
    @BeanField private boolean showBest = true;

    @BeanField private boolean showAgg = true;
    // END BEAN PROPERTIES

    private MessageLogger logger;

    @Override
    protected AggregateReceiver<CfgNRGPixelized> getAggregateReceiver() {
        return new AggregateReceiver<CfgNRGPixelized>() {

            @Override
            public void aggStart(
                    OptimizationFeedbackInitParams<CfgNRGPixelized> initParams, Aggregator agg) {
                logger = initParams.getInitContext().getLogger().messageLogger();
                MemoryUtilities.logMemoryUsage("MemoryUsageReporter step=start", logger);
            }

            @Override
            public void aggReport(Reporting<CfgNRGPixelized> reporting, Aggregator agg) {

                if (!showAgg) {
                    return;
                }

                MemoryUtilities.logMemoryUsage(
                        String.format("MemoryUsageReporter AGG step=%d", reporting.getIter()),
                        logger);
            }

            @Override
            public void aggEnd(Aggregator agg) {
                MemoryUtilities.logMemoryUsage("MemoryUsageReporter step=end", logger);
            }
        };
    }

    @Override
    public void reportNewBest(Reporting<CfgNRGPixelized> reporting) {

        if (!showBest) {
            return;
        }

        MemoryUtilities.logMemoryUsage(
                String.format("MemoryUsageReporter BEST step=%d", reporting.getIter()), logger);
    }

    public boolean isShowBest() {
        return showBest;
    }

    public void setShowBest(boolean showBest) {
        this.showBest = showBest;
    }

    public boolean isShowAgg() {
        return showAgg;
    }

    public void setShowAgg(boolean showAgg) {
        this.showAgg = showAgg;
    }
}
