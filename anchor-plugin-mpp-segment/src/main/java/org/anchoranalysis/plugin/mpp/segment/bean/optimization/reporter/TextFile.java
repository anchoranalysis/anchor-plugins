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

import java.io.PrintWriter;
import java.util.Optional;
import org.anchoranalysis.io.exception.AnchorIOException;
import org.anchoranalysis.io.generator.text.TextFileOutput;
import org.anchoranalysis.io.generator.text.TextFileOutputter;
import org.anchoranalysis.io.manifest.ManifestDescription;
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

public final class TextFile extends ReporterAgg<VoxelizedMarksWithEnergy>
        implements AggregateReceiver<VoxelizedMarksWithEnergy> {

    private Optional<TextFileOutput> fileOutput;

    private StopWatch timer = null;

    @Override
    public void aggStart(
            FeedbackBeginParameters<VoxelizedMarksWithEnergy> initParams, Aggregator agg)
            throws AggregatorException {
        fileOutput =
                TextFileOutputter.create(
                        "txt",
                        Optional.of(new ManifestDescription("text", "event_log")),
                        initParams.getInitContext().getOutputter().getChecked(),
                        "eventLog");
    }

    @Override
    public void reportBegin(FeedbackBeginParameters<VoxelizedMarksWithEnergy> initParams)
            throws ReporterException {

        super.reportBegin(initParams);
        try {
            if (fileOutput.isPresent()) {
                fileOutput.get().start();

                timer = new StopWatch();
                timer.start();
            }

        } catch (AnchorIOException e) {
            throw new ReporterException(e);
        }
    }

    @Override
    public void aggReport(Reporting<VoxelizedMarksWithEnergy> reporting, Aggregator agg) {
        if (fileOutput.isPresent() && fileOutput.get().isEnabled()) {

            fileOutput
                    .get()
                    .getWriter()
                    .printf(
                            "itr=%d  time=%e  tpi=%e   %s%n",
                            reporting.getIteration(),
                            ((double) timer.getTime()) / 1000,
                            ((double) timer.getTime()) / (reporting.getIteration() * 1000),
                            agg.toString());
        }
    }

    @Override
    public void reportNewBest(Reporting<VoxelizedMarksWithEnergy> reporting)
            throws ReporterException {
        if (fileOutput.isPresent() && fileOutput.get().isEnabled()) {

            fileOutput
                    .get()
                    .getWriter()
                    .printf(
                            "*** itr=%d  size=%d  best_energy=%e  kernel=%s%n",
                            reporting.getIteration(),
                            reporting.getMarksAfter().size(),
                            reporting.getMarksAfter().getEnergyTotal(),
                            reporting.kernelDescription());
        }
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
        if (fileOutput.isPresent() && fileOutput.get().isEnabled()) {

            timer.stop();

            PrintWriter writer = this.fileOutput.get().getWriter();
            writer.print(params.toString());
            writer.printf("Optimization time took %e s%n", ((double) timer.getTime()) / 1000);
            writer.close();
        }
    }
}
