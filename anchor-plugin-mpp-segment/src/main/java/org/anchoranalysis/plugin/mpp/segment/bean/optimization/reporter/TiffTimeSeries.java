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
import org.anchoranalysis.bean.shared.color.scheme.HSB;
import org.anchoranalysis.bean.shared.color.scheme.Shuffle;
import org.anchoranalysis.core.color.ColorIndex;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.identifier.getter.IdentifyByIteration;
import org.anchoranalysis.image.core.stack.DisplayStack;
import org.anchoranalysis.image.io.bean.object.draw.Outline;
import org.anchoranalysis.io.generator.Generator;
import org.anchoranalysis.io.generator.GeneratorBridge;
import org.anchoranalysis.io.generator.combined.CombinedListGenerator;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.mpp.feature.energy.marks.MarksWithEnergyBreakdown;
import org.anchoranalysis.mpp.feature.energy.marks.VoxelizedMarksWithEnergy;
import org.anchoranalysis.mpp.io.marks.ColoredMarksWithDisplayStack;
import org.anchoranalysis.mpp.io.marks.generator.MarksGenerator;
import org.anchoranalysis.mpp.mark.ColoredMarks;
import org.anchoranalysis.mpp.mark.MarkCollection;
import org.anchoranalysis.mpp.segment.bean.optimization.feedback.PeriodicSubdirectoryReporter;
import org.anchoranalysis.mpp.segment.optimization.feedback.FeedbackBeginParameters;
import org.anchoranalysis.mpp.segment.optimization.feedback.ReporterException;
import org.anchoranalysis.mpp.segment.optimization.step.Reporting;
import org.anchoranalysis.overlay.identifier.IdentifierFromOverlay;

public class TiffTimeSeries extends PeriodicSubdirectoryReporter<MarksWithEnergyBreakdown> {

    // START Bean Properties
    @BeanField @Getter @Setter private int numberColors = 20;
    // END Bean Properties

    private ColorIndex colorIndex;

    @Override
    public void reportBegin(FeedbackBeginParameters<VoxelizedMarksWithEnergy> initialization)
            throws ReporterException {

        try {
            colorIndex = new Shuffle(new HSB()).colorForEachIndex(numberColors);
        } catch (OperationFailedException e1) {
            throw new ReporterException(e1);
        }

        Generator<ColoredMarksWithDisplayStack> iterableRaster =
                new MarksGenerator(new Outline(), new IdentifierFromOverlay());

        // This no longer needs to be combined, it's a legacy of when a HTML reporter was attached
        //   cleaning up woould be nice
        CombinedListGenerator<MarksWithEnergyBreakdown> iterableCombined =
                new CombinedListGenerator<>(
                        GeneratorBridge.createOneToOne(
                                iterableRaster,
                                sourceObject -> addColor(sourceObject.getMarks(), initialization)));

        try {
            initialize(iterableCombined);
        } catch (OutputWriteFailedException e) {
            throw new ReporterException(e);
        }

        super.reportBegin(initialization);
    }

    @Override
    public void reportNewBest(Reporting<VoxelizedMarksWithEnergy> reporting) {
        // NOTHING TO DO
    }

    @Override
    protected Optional<MarksWithEnergyBreakdown> generateIterableElement(
            Reporting<VoxelizedMarksWithEnergy> reporting) throws ReporterException {
        return Optional.of(reporting.getMarksAfter().getMarks());
    }

    private ColoredMarksWithDisplayStack addColor(
            MarkCollection marks,
            FeedbackBeginParameters<VoxelizedMarksWithEnergy> initialization) {
        DisplayStack stack = initialization.getInitContext().getDualStack().getBackground();
        ColoredMarks coloredMarks =
                new ColoredMarks(marks, colorIndex, new IdentifyByIteration<>());
        return new ColoredMarksWithDisplayStack(coloredMarks, stack);
    }
}
