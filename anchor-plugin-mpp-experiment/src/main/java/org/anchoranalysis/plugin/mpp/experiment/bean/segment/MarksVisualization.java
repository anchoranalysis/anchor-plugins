/*-
 * #%L
 * anchor-plugin-mpp-experiment
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

package org.anchoranalysis.plugin.mpp.experiment.bean.segment;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.color.ColorIndex;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.idgetter.IDGetterIter;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.io.bean.object.writer.Filled;
import org.anchoranalysis.io.bean.object.writer.Outline;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.io.output.writer.WriterRouterErrors;
import org.anchoranalysis.mpp.io.marks.ColoredMarksWithDisplayStack;
import org.anchoranalysis.mpp.io.marks.generator.MarksGenerator;
import org.anchoranalysis.mpp.mark.ColoredMarks;
import org.anchoranalysis.mpp.mark.Mark;
import org.anchoranalysis.mpp.mark.MarkCollection;
import org.anchoranalysis.overlay.Overlay;
import org.anchoranalysis.overlay.bean.DrawObject;

/**
 * Maybe writes two raster visualizations of marks, one solid, and one with an outline
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class MarksVisualization {

    public static void write(
            MarkCollection marks,
            Outputter outputter,
            DisplayStack backgroundStack)
            throws OperationFailedException {
        ColorIndex colorIndex =
                outputter.getSettings().defaultColorIndexFor(marks.size());

        WriterRouterErrors writeIfAllowed = outputter.writerSelective();
        ColoredMarksWithDisplayStack marksWithStack =
                new ColoredMarksWithDisplayStack(
                        new ColoredMarks(marks, colorIndex, new IDGetterIter<Mark>()),
                        backgroundStack);

        writeMarksGenerator(writeIfAllowed, "solid", new Filled(), marksWithStack);
        writeMarksGenerator(writeIfAllowed, "outline", new Outline(), marksWithStack);
    }

    private static void writeMarksGenerator(
            WriterRouterErrors writeIfAllowed,
            String outputName,
            DrawObject drawObject,
            ColoredMarksWithDisplayStack marksWithStack) {
        writeIfAllowed.write(
                outputName,
                () -> new MarksGenerator(drawObject, marksWithStack, new IDGetterIter<Overlay>()));
    }
}
