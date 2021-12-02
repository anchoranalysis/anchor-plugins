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

import java.util.Optional;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.io.generator.text.TextFileOutput;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.mpp.feature.energy.marks.VoxelizedMarksWithEnergy;
import org.anchoranalysis.mpp.segment.bean.optimization.feedback.FeedbackReceiverBean;
import org.anchoranalysis.mpp.segment.optimization.feedback.FeedbackBeginParameters;
import org.anchoranalysis.mpp.segment.optimization.feedback.FeedbackEndParameters;
import org.anchoranalysis.mpp.segment.optimization.feedback.ReporterException;
import org.anchoranalysis.mpp.segment.optimization.step.Reporting;

/**
 * Outputs a CSV with statistics on the configuration/energy <b>only</b> the <i>best
 * configuration</i> state changes.
 *
 * @author Owen Feehan
 */
public class CSVReporterBest extends FeedbackReceiverBean<VoxelizedMarksWithEnergy> {

    public static final String MANIFEST_FUNCTION = "event_aggregate_stats";

    private static final String OUTPUT_CSV_STATISTICS = "statisticsBest";

    private Optional<TextFileOutput> csvOutput;

    @Override
    public void reportItr(Reporting<VoxelizedMarksWithEnergy> reporting) {
        // NOTHING TO DO
    }

    @Override
    public void reportNewBest(Reporting<VoxelizedMarksWithEnergy> reporting)
            throws ReporterException {
        if (csvOutput.isPresent() && csvOutput.get().isEnabled()) {

            this.csvOutput
                    .get()
                    .getWriter()
                    .printf(
                            "%d,%d,%e%n",
                            reporting.getIteration(),
                            reporting.getMarksAfter().size(),
                            reporting.getMarksAfter().getEnergyTotal());
        }
    }

    @Override
    public void reportBegin(FeedbackBeginParameters<VoxelizedMarksWithEnergy> initialization)
            throws ReporterException {

        try {
            this.csvOutput = createOutput(initialization);
            OptionalUtilities.ifPresent(
                    csvOutput,
                    output -> {
                        output.start();
                        output.getWriter().printf("Itr,Size,Best_Energy%n");
                    });
        } catch (OutputWriteFailedException e) {
            throw new ReporterException(e);
        }
    }

    @Override
    public void reportEnd(FeedbackEndParameters<VoxelizedMarksWithEnergy> parameters) {
        csvOutput.ifPresent(output -> output.getWriter().close());
    }

    private Optional<TextFileOutput> createOutput(
            FeedbackBeginParameters<VoxelizedMarksWithEnergy> initialization) {
        return CSVReporterUtilities.createFileOutputFor(
                OUTPUT_CSV_STATISTICS, initialization, MANIFEST_FUNCTION);
    }
}
