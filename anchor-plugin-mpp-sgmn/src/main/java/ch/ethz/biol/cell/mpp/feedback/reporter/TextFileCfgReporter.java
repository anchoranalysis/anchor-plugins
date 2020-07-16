/* (C)2020 */
package ch.ethz.biol.cell.mpp.feedback.reporter;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.feedback.PeriodicSubfolderReporter;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackInitParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.ReporterException;
import org.anchoranalysis.mpp.sgmn.optscheme.step.Reporting;

public class TextFileCfgReporter extends PeriodicSubfolderReporter<Cfg> {

    public TextFileCfgReporter() {
        super();
    }

    @Override
    public void reportBegin(OptimizationFeedbackInitParams<CfgNRGPixelized> initParams)
            throws ReporterException {

        super.reportBegin(initParams);

        try {
            init(new ObjectAsStringGenerator<Cfg>());
        } catch (OutputWriteFailedException e) {
            throw new ReporterException(e);
        }
    }

    @Override
    protected Optional<Cfg> generateIterableElement(Reporting<CfgNRGPixelized> reporting)
            throws ReporterException {
        return Optional.of(reporting.getCfgNRGAfter().getCfg());
    }

    @Override
    public void reportNewBest(Reporting<CfgNRGPixelized> reporting) {
        // NOTHING TO DO
    }
}
