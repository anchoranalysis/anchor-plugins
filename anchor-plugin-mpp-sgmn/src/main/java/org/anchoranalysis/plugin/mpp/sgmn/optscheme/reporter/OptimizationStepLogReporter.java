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

import org.anchoranalysis.anchor.mpp.feature.energy.marks.VoxelizedMarksWithEnergy;
import org.anchoranalysis.core.log.MessageLogger;
import org.anchoranalysis.mpp.segment.bean.optscheme.feedback.ReporterAgg;
import org.anchoranalysis.mpp.segment.optscheme.feedback.OptimizationFeedbackEndParams;
import org.anchoranalysis.mpp.segment.optscheme.feedback.OptimizationFeedbackInitParams;
import org.anchoranalysis.mpp.segment.optscheme.feedback.ReporterException;
import org.anchoranalysis.mpp.segment.optscheme.feedback.aggregate.AggregateReceiver;
import org.anchoranalysis.mpp.segment.optscheme.feedback.aggregate.Aggregator;
import org.anchoranalysis.mpp.segment.optscheme.step.Reporting;
import org.apache.commons.lang.time.StopWatch;

public final class OptimizationStepLogReporter extends ReporterAgg<VoxelizedMarksWithEnergy>
        implements AggregateReceiver<VoxelizedMarksWithEnergy> {

    private StopWatch timer;
    private MessageLogger logger;

    @Override
    public void reportBegin(OptimizationFeedbackInitParams<VoxelizedMarksWithEnergy> initParams)
            throws ReporterException {

        super.reportBegin(initParams);

        timer = new StopWatch();
        timer.start();

        logger = initParams.getInitContext().getLogger().messageLogger();
    }

    @Override
    public void aggStart(
            OptimizationFeedbackInitParams<VoxelizedMarksWithEnergy> initParams, Aggregator agg) {
        // NOTHING TO DO
    }

    @Override
    public void reportNewBest(Reporting<VoxelizedMarksWithEnergy> reporting) throws ReporterException {

        logger.logFormatted(
                "*** itr=%d  size=%d  best_energy=%e  kernel=%s",
                reporting.getIter(),
                reporting.getMarksAfter().size(),
                reporting.getMarksAfter().getEnergyTotal(),
                reporting.getKernel().getDescription());
    }

    @Override
    public void aggReport(Reporting<VoxelizedMarksWithEnergy> reporting, Aggregator agg) {

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
    public void reportEnd(OptimizationFeedbackEndParams<VoxelizedMarksWithEnergy> optStep) {

        timer.stop();

        optStep.getLogReporter().log(optStep.getState().toString());
        optStep.getLogReporter()
                .logFormatted("Optimization time took %e s%n", ((double) timer.getTime()) / 1000);

        super.reportEnd(optStep);
    }

    @Override
    protected AggregateReceiver<VoxelizedMarksWithEnergy> getAggregateReceiver() {
        return this;
    }
}
