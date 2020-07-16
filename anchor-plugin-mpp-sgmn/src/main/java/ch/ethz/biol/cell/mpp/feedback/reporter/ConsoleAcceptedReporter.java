/* (C)2020 */
package ch.ethz.biol.cell.mpp.feedback.reporter;

import java.util.Optional;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRG;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.feedback.FeedbackReceiverBean;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackEndParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackInitParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.ReporterException;
import org.anchoranalysis.mpp.sgmn.optscheme.step.Reporting;

public class ConsoleAcceptedReporter extends FeedbackReceiverBean<CfgNRGPixelized> {

    private Logger logger;

    public ConsoleAcceptedReporter() {
        super();
    }

    @Override
    public void reportBegin(OptimizationFeedbackInitParams<CfgNRGPixelized> initParams)
            throws ReporterException {
        this.logger = initParams.getInitContext().getLogger();
    }

    @Override
    public void reportItr(Reporting<CfgNRGPixelized> reporting) {

        if (reporting.isAccptd()) {

            logger.messageLogger()
                    .logFormatted(
                            "itr=%5d  size=%3d  nrg=%e  best_nrg=%e   kernel=%s",
                            reporting.getIter(),
                            extractStatInt(
                                    reporting.getCfgNRGAfterOptional(), a -> a.getCfg().size()),
                            extractStatDbl(reporting.getCfgNRGAfterOptional(), CfgNRG::getNrgTotal),
                            extractStatDbl(reporting.getBest(), CfgNRG::getNrgTotal),
                            reporting.getKernel().getDescription());
        }
    }

    private static double extractStatDbl(
            Optional<CfgNRGPixelized> cfgNRG, ToDoubleFunction<CfgNRG> func) {
        if (cfgNRG.isPresent()) {
            return func.applyAsDouble(cfgNRG.get().getCfgNRG());
        } else {
            return Double.NaN;
        }
    }

    private static int extractStatInt(
            Optional<CfgNRGPixelized> cfgNRG, ToIntFunction<CfgNRG> func) {
        if (cfgNRG.isPresent()) {
            return func.applyAsInt(cfgNRG.get().getCfgNRG());
        } else {
            return Integer.MIN_VALUE;
        }
    }

    @Override
    public void reportEnd(OptimizationFeedbackEndParams<CfgNRGPixelized> optStep) {
        // NOTHING TO DO
    }

    @Override
    public void reportNewBest(Reporting<CfgNRGPixelized> reporting) {
        // NOTHING TO DO
    }
}
