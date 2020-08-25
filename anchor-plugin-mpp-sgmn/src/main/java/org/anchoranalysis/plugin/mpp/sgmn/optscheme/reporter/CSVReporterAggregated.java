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

import java.io.PrintWriter;
import java.util.Optional;
import org.anchoranalysis.anchor.mpp.feature.energy.marks.VoxelizedMarksWithEnergy;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.output.file.FileOutput;
import org.anchoranalysis.mpp.segment.bean.optscheme.feedback.ReporterAgg;
import org.anchoranalysis.mpp.segment.optscheme.feedback.OptimizationFeedbackEndParams;
import org.anchoranalysis.mpp.segment.optscheme.feedback.OptimizationFeedbackInitParams;
import org.anchoranalysis.mpp.segment.optscheme.feedback.ReporterException;
import org.anchoranalysis.mpp.segment.optscheme.feedback.aggregate.AggregateReceiver;
import org.anchoranalysis.mpp.segment.optscheme.feedback.aggregate.Aggregator;
import org.anchoranalysis.mpp.segment.optscheme.feedback.aggregate.AggregatorException;
import org.anchoranalysis.mpp.segment.optscheme.step.Reporting;
import org.apache.commons.lang.time.StopWatch;

public class CSVReporterAggregated extends ReporterAgg<VoxelizedMarksWithEnergy>
        implements AggregateReceiver<VoxelizedMarksWithEnergy> {

    private Optional<FileOutput> csvOutput;

    private StopWatch timer = null;

    private long crntTime;

    private long lastTime;

    private long totalCount;

    // N.B.
    //  we can make this faster by only doing the aggregation if necessary
    //  we probably should separate the two types of csv writing, as they have effectively nothing
    // in common

    @Override
    protected AggregateReceiver<VoxelizedMarksWithEnergy> getAggregateReceiver() {
        return this;
    }

    @Override
    public void reportBegin(OptimizationFeedbackInitParams<VoxelizedMarksWithEnergy> initParams)
            throws ReporterException {

        super.reportBegin(initParams);

        this.csvOutput =
                CSVReporterUtilities.createFileOutputFor(
                        "csvStatsAgg", initParams, "interval_aggregate_stats");

        timer = new StopWatch();
        timer.start();

        crntTime = 0;
        lastTime = 0;
        totalCount = 0;
    }

    @Override
    public void aggStart(
            OptimizationFeedbackInitParams<VoxelizedMarksWithEnergy> initParams, Aggregator agg)
            throws AggregatorException {

        try {
            OptionalUtilities.ifPresent(
                    csvOutput,
                    output -> {
                        output.start();
                        PrintWriter writer = output.getWriter();
                        writer.print("Itr,");
                        agg.outputHeaderToWriter(writer);
                        writer.print(",Time,TimePerIter,IntervalTimePerIter");
                        writer.println();
                    });
        } catch (AnchorIOException e) {
            throw new AggregatorException(e);
        }
    }

    @Override
    public void aggReport(Reporting<VoxelizedMarksWithEnergy> reporting, Aggregator agg) {
        csvOutput.ifPresent(
                output -> {

                    // Shift time
                    this.lastTime = this.crntTime;
                    this.crntTime = this.timer.getTime();

                    totalCount++;
                    long totalItr = getAggInterval() * totalCount;

                    PrintWriter writer = output.getWriter();
                    writer.printf("%d,", totalItr);

                    agg.outputToWriter(writer);

                    writer.printf(
                            ",%e,%e,%e",
                            toSeconds(this.crntTime),
                            toSeconds(this.crntTime) / totalItr,
                            toSeconds((this.crntTime - this.lastTime)) / getAggInterval());
                    writer.println();
                    writer.flush();
                });
    }

    @Override
    public void aggEnd(Aggregator agg) {
        // NOTHING TO DO
    }

    @Override
    public void reportEnd(OptimizationFeedbackEndParams<VoxelizedMarksWithEnergy> optStep) {
        super.reportEnd(optStep);
        timer.stop();
        csvOutput.ifPresent(FileOutput::end);
    }

    @Override
    public void reportNewBest(Reporting<VoxelizedMarksWithEnergy> reporting) {
        // NOTHING TO DO
    }

    private static double toSeconds(long time) {
        return (time / 1000);
    }
}
