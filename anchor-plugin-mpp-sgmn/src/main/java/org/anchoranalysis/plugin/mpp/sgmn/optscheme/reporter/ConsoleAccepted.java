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

package org.anchoranalysis.plugin.mpp.sgmn.optscheme.reporter;

import java.util.Optional;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import org.anchoranalysis.anchor.mpp.feature.energy.marks.MarksWithEnergyBreakdown;
import org.anchoranalysis.anchor.mpp.feature.energy.marks.VoxelizedMarksWithEnergy;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.feedback.FeedbackReceiverBean;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackEndParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackInitParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.ReporterException;
import org.anchoranalysis.mpp.sgmn.optscheme.step.Reporting;

public class ConsoleAccepted extends FeedbackReceiverBean<VoxelizedMarksWithEnergy> {

    private Logger logger;

    @Override
    public void reportBegin(OptimizationFeedbackInitParams<VoxelizedMarksWithEnergy> initParams)
            throws ReporterException {
        this.logger = initParams.getInitContext().getLogger();
    }

    @Override
    public void reportItr(Reporting<VoxelizedMarksWithEnergy> reporting) {

        if (reporting.isAccepted()) {

            logger.messageLogger()
                    .logFormatted(
                            "itr=%5d  size=%3d  energy=%e  best_energy=%e   kernel=%s",
                            reporting.getIter(),
                            extractStatisticInt(
                                    reporting.getMarksAfterOptional(), MarksWithEnergyBreakdown::size),
                            extractStatisticDouble(reporting.getMarksAfterOptional(), MarksWithEnergyBreakdown::getEnergyTotal),
                            extractStatisticDouble(reporting.getBest(), MarksWithEnergyBreakdown::getEnergyTotal),
                            reporting.getKernel().getDescription());
        }
    }

    private static double extractStatisticDouble(
            Optional<VoxelizedMarksWithEnergy> marks, ToDoubleFunction<MarksWithEnergyBreakdown> func) {
        if (marks.isPresent()) {
            return func.applyAsDouble(marks.get().getMarks());
        } else {
            return Double.NaN;
        }
    }

    private static int extractStatisticInt(
            Optional<VoxelizedMarksWithEnergy> marks, ToIntFunction<MarksWithEnergyBreakdown> func) {
        if (marks.isPresent()) {
            return func.applyAsInt(marks.get().getMarks());
        } else {
            return Integer.MIN_VALUE;
        }
    }

    @Override
    public void reportEnd(OptimizationFeedbackEndParams<VoxelizedMarksWithEnergy> optStep) {
        // NOTHING TO DO
    }

    @Override
    public void reportNewBest(Reporting<VoxelizedMarksWithEnergy> reporting) {
        // NOTHING TO DO
    }
}
