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
import org.anchoranalysis.io.generator.serialized.BundledObjectOutputStreamGenerator;
import org.anchoranalysis.io.manifest.ManifestDescription;
import org.anchoranalysis.io.manifest.deserializer.bundle.BundleParameters;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.mpp.feature.energy.marks.MarksWithEnergyBreakdown;
import org.anchoranalysis.mpp.feature.energy.marks.VoxelizedMarksWithEnergy;
import org.anchoranalysis.mpp.segment.bean.optimization.feedback.FeedbackReceiverBean;
import org.anchoranalysis.mpp.segment.optimization.feedback.FeedbackBeginParameters;
import org.anchoranalysis.mpp.segment.optimization.feedback.FeedbackEndParameters;
import org.anchoranalysis.mpp.segment.optimization.feedback.ReporterException;
import org.anchoranalysis.mpp.segment.optimization.step.Reporting;

public class VoxelizedMarksChangeReporter extends FeedbackReceiverBean<VoxelizedMarksWithEnergy> {

    // START BEAN PARAMETERS
    @BeanField @Getter @Setter private String manifestFunction = "marks";

    @BeanField @Getter @Setter private String outputName;

    @BeanField @Getter @Setter private int bundleSize = 1000;

    @BeanField @Getter @Setter private boolean best = false;
    // END BEAN PARAMETERS

    private OutputSequenceIndexed<MarksWithEnergyBreakdown,Integer> outputSequence;

    private Reporting<VoxelizedMarksWithEnergy> lastOptimizationStep;

    @Override
    public void reportBegin(FeedbackBeginParameters<VoxelizedMarksWithEnergy> initParams)
            throws ReporterException {

        OutputPatternIntegerSuffix pattern = new OutputPatternIntegerSuffix(
                outputName,
                10,
                true,
                Optional.of(new ManifestDescription("serialized", manifestFunction))
            );                        
        
        try {
            outputSequence = createSequenceFactory(initParams).increasingIntegers(pattern);    
        } catch (OutputWriteFailedException e) {
            throw new ReporterException(e);
        }
    }

    @Override
    public void reportItr(Reporting<VoxelizedMarksWithEnergy> reporting) throws ReporterException {
        try {
            if (reporting.isAccepted() && outputSequence != null) {
                addToSequenceWriter(
                        best ? reporting.getBestState() : reporting.getMarksAfterOptional(),
                        reporting.getIteration());
            }
            lastOptimizationStep = reporting;
        } catch (OutputWriteFailedException e) {
            throw new ReporterException(e);
        }
    }

    @Override
    public void reportEnd(FeedbackEndParameters<VoxelizedMarksWithEnergy> params)
            throws ReporterException {

        if (outputSequence == null) {
            return;
        }

        if (outputSequence.isOn() && lastOptimizationStep != null) {
            outputSequence.getSequenceType().assignMaximumIndex(lastOptimizationStep.getIteration());
        }

        try {
            outputSequence.close();
        } catch (OutputWriteFailedException e) {
            throw new ReporterException(e);
        }
    }

    @Override
    public void reportNewBest(Reporting<VoxelizedMarksWithEnergy> reporting) {
        // NOTHING TO DO
    }
    
    private OutputSequenceFactory<MarksWithEnergyBreakdown> createSequenceFactory(FeedbackBeginParameters<VoxelizedMarksWithEnergy> initParams) throws OutputWriteFailedException {
        
        Generator<MarksWithEnergyBreakdown> generator =
                new BundledObjectOutputStreamGenerator<>(
                        createBundleParameters(),
                        outputName,
                        10,
                        initParams.getInitContext().getInputOutputContext(),
                        manifestFunction);

        return new OutputSequenceFactory<>(generator, initParams.getInitContext().getInputOutputContext());
    }
    
    private BundleParameters createBundleParameters() {
        BundleParameters bundleParams = new BundleParameters();
        bundleParams.setBundleSize(bundleSize);
        bundleParams.setSequenceType(outputSequence.getSequenceType());
        return bundleParams;
    }

    private void addToSequenceWriter(Optional<VoxelizedMarksWithEnergy> marks, int iteration)
            throws OutputWriteFailedException {
        if (marks.isPresent()) {
            outputSequence.add(marks.get().getMarks(), iteration);
        }
    }
}
