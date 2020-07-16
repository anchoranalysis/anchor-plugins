/* (C)2020 */
package ch.ethz.biol.cell.mpp.feedback.reporter;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.generator.serialized.XStreamGenerator;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.feedback.FeedbackReceiverBean;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackEndParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackInitParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.ReporterException;
import org.anchoranalysis.mpp.sgmn.optscheme.step.Reporting;
import org.apache.commons.lang.time.StopWatch;

public class MinimalExecutionTimeStatsReporter extends FeedbackReceiverBean<CfgNRGPixelized> {

    // START BEAN PROPERTIES
    @BeanField private String outputName = "minimalExecutionTimeStats";
    // END BEAN PROPERTIES

    private BoundOutputManagerRouteErrors outputManager = null;
    private KernelExecutionStats stats;
    private StopWatch stopWatch;

    @Override
    public void reportBegin(OptimizationFeedbackInitParams<CfgNRGPixelized> initParams)
            throws ReporterException {
        outputManager = initParams.getInitContext().getOutputManager();
        stats = new KernelExecutionStats(initParams.getKernelFactoryList().size());
        stopWatch = new StopWatch();
        stopWatch.start();
    }

    @Override
    public void reportItr(Reporting<CfgNRGPixelized> reporting) {

        int kernelID = reporting.getKernel().getID();
        double executionTime = reporting.getExecutionTime();

        if (reporting.getProposal().isPresent()) {

            if (reporting.isAccptd()) {
                stats.incrAccepted(kernelID, executionTime);
            } else {
                stats.incrRejected(kernelID, executionTime);
            }
        } else {
            // No proposal
            stats.incrNotProposed(kernelID, executionTime);
        }
    }

    @Override
    public void reportNewBest(Reporting<CfgNRGPixelized> reporting) {
        // NOTHING TO DO
    }

    @Override
    public void reportEnd(OptimizationFeedbackEndParams<CfgNRGPixelized> optStep) {
        stats.setTotalExecutionTime(stopWatch.getTime());
        stopWatch.stop();

        outputManager
                .getWriterCheckIfAllowed()
                .write(
                        outputName,
                        () ->
                                new XStreamGenerator<>(
                                        stats, Optional.of("minimalExecutionTimeStats")));
    }

    public String getOutputName() {
        return outputName;
    }

    public void setOutputName(String outputName) {
        this.outputName = outputName;
    }
}
