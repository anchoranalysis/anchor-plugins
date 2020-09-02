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
import org.anchoranalysis.io.generator.IterableGenerator;
import org.anchoranalysis.io.generator.sequence.GeneratorSequenceNonIncrementalWriter;
import org.anchoranalysis.io.generator.serialized.BundledObjectOutputStreamGenerator;
import org.anchoranalysis.io.manifest.ManifestDescription;
import org.anchoranalysis.io.manifest.deserializer.bundle.BundleParameters;
import org.anchoranalysis.io.manifest.sequencetype.ChangeSequenceType;
import org.anchoranalysis.io.namestyle.IndexableOutputNameStyle;
import org.anchoranalysis.io.namestyle.IntegerSuffixOutputNameStyle;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.mpp.feature.energy.marks.MarksWithEnergyBreakdown;
import org.anchoranalysis.mpp.feature.energy.marks.VoxelizedMarksWithEnergy;
import org.anchoranalysis.mpp.segment.bean.optimization.feedback.FeedbackReceiverBean;
import org.anchoranalysis.mpp.segment.optimization.feedback.FeedbackEndParameters;
import org.anchoranalysis.mpp.segment.optimization.feedback.FeedbackBeginParameters;
import org.anchoranalysis.mpp.segment.optimization.feedback.ReporterException;
import org.anchoranalysis.mpp.segment.optimization.step.Reporting;

public class VoxelizedMarksChangeReporter extends FeedbackReceiverBean<VoxelizedMarksWithEnergy> {

    // START BEAN PARAMETERS
    @BeanField @Getter @Setter private String manifestFunction = "marks";

    @BeanField @Getter @Setter private String outputName;

    @BeanField @Getter @Setter private int bundleSize = 1000;

    @BeanField @Getter @Setter private boolean best = false;
    // END BEAN PARAMETERS

    private GeneratorSequenceNonIncrementalWriter<MarksWithEnergyBreakdown> sequenceWriter;

    private ChangeSequenceType sequenceType;

    private Reporting<VoxelizedMarksWithEnergy> lastOptimizationStep;

    @Override
    public void reportBegin(FeedbackBeginParameters<VoxelizedMarksWithEnergy> initParams)
            throws ReporterException {

        BoundOutputManagerRouteErrors outputManager =
                initParams.getInitContext().getOutputManager();

        sequenceType = new ChangeSequenceType();

        BundleParameters bundleParams = createBundleParameters();

        IterableGenerator<MarksWithEnergyBreakdown> iterableGenerator =
                new BundledObjectOutputStreamGenerator<>(
                        bundleParams,
                        this.generateOutputNameStyle(),
                        outputManager.getDelegate(),
                        manifestFunction);

        IndexableOutputNameStyle outputNameStyle = generateOutputNameStyle();
        sequenceWriter =
                new GeneratorSequenceNonIncrementalWriter<>(
                        outputManager.getDelegate(),
                        outputNameStyle.getOutputName(),
                        outputNameStyle,
                        iterableGenerator,
                        true,
                        new ManifestDescription("serialized", manifestFunction));

        try {
            sequenceWriter.start(sequenceType, -1);
        } catch (OutputWriteFailedException e) {
            throw new ReporterException(e);
        }
    }

    @Override
    public void reportItr(Reporting<VoxelizedMarksWithEnergy> reporting) throws ReporterException {
        try {
            if (reporting.isAccepted() && sequenceWriter != null) {
                addToSequenceWriter(
                        best ? reporting.getBest() : reporting.getMarksAfterOptional(),
                        reporting.getIter());
            }
            lastOptimizationStep = reporting;
        } catch (OutputWriteFailedException e) {
            throw new ReporterException(e);
        }
    }

    @Override
    public void reportEnd(FeedbackEndParameters<VoxelizedMarksWithEnergy> params)
            throws ReporterException {

        if (sequenceWriter == null) {
            return;
        }

        if (sequenceWriter.isOn() && lastOptimizationStep != null) {
            sequenceType.setMaximumIndex(lastOptimizationStep.getIter());
        }

        try {
            sequenceWriter.end();
        } catch (OutputWriteFailedException e) {
            throw new ReporterException(e);
        }
    }

    @Override
    public void reportNewBest(Reporting<VoxelizedMarksWithEnergy> reporting) {
        // NOTHING TO DO
    }

    // We generate an OutputName class from the outputName string
    private IndexableOutputNameStyle generateOutputNameStyle() {
        return new IntegerSuffixOutputNameStyle(outputName, 10);
    }

    private BundleParameters createBundleParameters() {
        BundleParameters bundleParams = new BundleParameters();
        bundleParams.setBundleSize(bundleSize);
        bundleParams.setSequenceType(sequenceType);
        return bundleParams;
    }

    private void addToSequenceWriter(Optional<VoxelizedMarksWithEnergy> marks, int iter)
            throws OutputWriteFailedException {
        if (marks.isPresent()) {
            sequenceWriter.add(marks.get().getMarks(), String.valueOf(iter));
        }
    }
}
