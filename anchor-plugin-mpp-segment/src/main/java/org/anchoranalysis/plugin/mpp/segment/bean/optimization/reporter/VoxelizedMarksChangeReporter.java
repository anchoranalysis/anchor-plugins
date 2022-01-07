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
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.generator.Generator;
import org.anchoranalysis.io.generator.sequence.OutputSequenceFactory;
import org.anchoranalysis.io.generator.sequence.OutputSequenceIndexed;
import org.anchoranalysis.io.generator.sequence.pattern.OutputPatternIntegerSuffix;
import org.anchoranalysis.io.generator.serialized.ObjectOutputStreamGenerator;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.outputter.OutputterChecked;
import org.anchoranalysis.mpp.feature.energy.marks.MarksWithEnergyBreakdown;
import org.anchoranalysis.mpp.feature.energy.marks.VoxelizedMarksWithEnergy;
import org.anchoranalysis.mpp.segment.bean.optimization.feedback.FeedbackReceiverBean;
import org.anchoranalysis.mpp.segment.optimization.feedback.FeedbackBeginParameters;
import org.anchoranalysis.mpp.segment.optimization.feedback.FeedbackEndParameters;
import org.anchoranalysis.mpp.segment.optimization.feedback.ReporterException;
import org.anchoranalysis.mpp.segment.optimization.step.Reporting;

public class VoxelizedMarksChangeReporter extends FeedbackReceiverBean<VoxelizedMarksWithEnergy> {

    // START BEAN PARAMETERS
    @BeanField @Getter @Setter private String outputName;

    @BeanField @Getter @Setter private int bundleSize = 1000;

    @BeanField @Getter @Setter private boolean best = false;
    // END BEAN PARAMETERS

    private OutputSequenceIndexed<MarksWithEnergyBreakdown, Integer> outputSequence;

    @Override
    public void reportBegin(FeedbackBeginParameters<VoxelizedMarksWithEnergy> initialization)
            throws ReporterException {

        OutputPatternIntegerSuffix pattern = new OutputPatternIntegerSuffix(outputName, 10, true);

        try {
            outputSequence = createSequenceFactory(initialization).indexedWithInteger(pattern);
        } catch (OutputWriteFailedException e) {
            throw new ReporterException(e);
        }
    }

    @Override
    public void reportIteration(Reporting<VoxelizedMarksWithEnergy> reporting)
            throws ReporterException {
        try {
            if (reporting.isAccepted() && outputSequence != null) {
                addToSequenceWriter(
                        best ? reporting.getBestState() : reporting.getMarksAfterOptional(),
                        reporting.getIteration());
            }
        } catch (OutputWriteFailedException e) {
            throw new ReporterException(e);
        }
    }

    @Override
    public void reportEnd(FeedbackEndParameters<VoxelizedMarksWithEnergy> parameters)
            throws ReporterException {
        // NOTHING TO DO
    }

    @Override
    public void reportNewBest(Reporting<VoxelizedMarksWithEnergy> reporting) {
        // NOTHING TO DO
    }

    private OutputSequenceFactory<MarksWithEnergyBreakdown> createSequenceFactory(
            FeedbackBeginParameters<VoxelizedMarksWithEnergy> initialization) {

        OutputterChecked outputter = initialization.getInitContext().getOutputter().getChecked();

        Generator<MarksWithEnergyBreakdown> generator = new ObjectOutputStreamGenerator<>();

        return new OutputSequenceFactory<>(generator, outputter);
    }

    private void addToSequenceWriter(Optional<VoxelizedMarksWithEnergy> marks, int iteration)
            throws OutputWriteFailedException {
        if (marks.isPresent()) {
            outputSequence.add(marks.get().getMarks(), iteration);
        }
    }
}
