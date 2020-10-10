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

package org.anchoranalysis.plugin.mpp.segment.bean.optimization.reporter;

import lombok.NoArgsConstructor;
import org.anchoranalysis.mpp.feature.energy.marks.VoxelizedMarksWithEnergy;
import org.anchoranalysis.mpp.segment.bean.optimization.feedback.ReporterAgg;
import org.anchoranalysis.mpp.segment.optimization.feedback.FeedbackBeginParameters;
import org.anchoranalysis.mpp.segment.optimization.feedback.FeedbackEndParameters;
import org.anchoranalysis.mpp.segment.optimization.feedback.ReporterException;
import org.anchoranalysis.mpp.segment.optimization.feedback.aggregate.AggregateReceiver;
import org.anchoranalysis.mpp.segment.optimization.feedback.aggregate.Aggregator;
import org.anchoranalysis.mpp.segment.optimization.feedback.aggregate.AggregatorException;
import org.anchoranalysis.mpp.segment.optimization.step.Reporting;
import org.apache.commons.lang.time.StopWatch;

@NoArgsConstructor
public final class ConsoleAggregatedReporter extends ReporterAgg<VoxelizedMarksWithEnergy>
        implements AggregateReceiver<VoxelizedMarksWithEnergy> {

    private StopWatch timer = null;

    public ConsoleAggregatedReporter(double aggIntervalLog10) {
        super(aggIntervalLog10);
    }

    @Override
    public void aggStart(
            FeedbackBeginParameters<VoxelizedMarksWithEnergy> initParams, Aggregator agg)
            throws AggregatorException {
        // NOTHING TO DO
    }

    @Override
    public void reportBegin(FeedbackBeginParameters<VoxelizedMarksWithEnergy> initParams)
            throws ReporterException {
        super.reportBegin(initParams);
        timer = new StopWatch();
        timer.start();
    }

    @Override
    public void aggReport(Reporting<VoxelizedMarksWithEnergy> reporting, Aggregator agg) {
        System.out.printf( // NOSONAR
                "itr=%d  time=%e  tpi=%e   %s%n",
                reporting.getIteration(),
                ((double) timer.getTime()) / 1000,
                ((double) timer.getTime()) / (reporting.getIteration() * 1000),
                agg.toString());
    }

    @Override
    public void reportNewBest(Reporting<VoxelizedMarksWithEnergy> reporting)
            throws ReporterException {
        System.out.printf( // NOSONAR
                "*** itr=%d  size=%d  best_energy=%e  kernel=%s%n",
                reporting.getIteration(),
                reporting.getMarksAfter().size(),
                reporting.getMarksAfter().getEnergyTotal(),
                reporting.kernelDescription());
    }

    @Override
    public void aggEnd(Aggregator agg) {
        // NOTHING TO DO
    }

    @Override
    protected AggregateReceiver<VoxelizedMarksWithEnergy> getAggregateReceiver() {
        return this;
    }

    @Override
    public void reportEnd(FeedbackEndParameters<VoxelizedMarksWithEnergy> params) {
        super.reportEnd(params);
        timer.stop();
        System.out.printf( // NOSONAR
                "Optimization time took %e s%n", ((double) timer.getTime()) / 1000);
    }
}
