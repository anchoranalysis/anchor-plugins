/* (C)2020 */
package ch.ethz.biol.cell.mpp.feedback.reporter;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.file.FileOutput;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.feedback.FeedbackReceiverBean;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackEndParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackInitParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.ReporterException;
import org.anchoranalysis.mpp.sgmn.optscheme.step.Reporting;

public class CSVReporterBest extends FeedbackReceiverBean<CfgNRGPixelized> {

    private Optional<FileOutput> csvOutput;

    @Override
    public void reportItr(Reporting<CfgNRGPixelized> reporting) {
        // NOTHING TO DO
    }

    @Override
    public void reportNewBest(Reporting<CfgNRGPixelized> reporting) throws ReporterException {
        if (csvOutput.isPresent() && csvOutput.get().isEnabled()) {

            this.csvOutput
                    .get()
                    .getWriter()
                    .printf(
                            "%d,%d,%e%n",
                            reporting.getIter(),
                            reporting.getCfgNRGAfter().getCfg().size(),
                            reporting.getCfgNRGAfter().getCfgNRG().getNrgTotal());
        }
    }

    @Override
    public void reportBegin(OptimizationFeedbackInitParams<CfgNRGPixelized> initParams)
            throws ReporterException {

        try {
            this.csvOutput = createOutput(initParams);
            OptionalUtilities.ifPresent(
                    csvOutput,
                    output -> {
                        output.start();
                        output.getWriter().printf("Itr,Size,Best_Nrg%n");
                    });
        } catch (AnchorIOException e) {
            throw new ReporterException(e);
        }
    }

    @Override
    public void reportEnd(OptimizationFeedbackEndParams<CfgNRGPixelized> optStep) {
        csvOutput.ifPresent(output -> output.getWriter().close());
    }

    private Optional<FileOutput> createOutput(
            OptimizationFeedbackInitParams<CfgNRGPixelized> initParams) throws ReporterException {
        try {
            return CSVReporterUtilities.createFileOutputFor(
                    "csvStatsBest", initParams, "event_aggregate_stats");
        } catch (OutputWriteFailedException e) {
            throw new ReporterException(e);
        }
    }
}
