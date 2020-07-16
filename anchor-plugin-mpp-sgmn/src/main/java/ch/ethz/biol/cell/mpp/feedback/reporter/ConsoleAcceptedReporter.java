/*-
 * #%L
 * anchor-plugin-mpp-sgmn
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
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
