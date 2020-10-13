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

import lombok.AllArgsConstructor;
import java.util.function.Supplier;
import org.anchoranalysis.core.cache.CachedSupplier;
import org.anchoranalysis.core.color.ColorIndex;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.idgetter.IDGetterIter;
import org.anchoranalysis.image.io.bean.object.draw.Filled;
import org.anchoranalysis.image.io.bean.object.draw.Outline;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.io.output.writer.ElementSupplier;
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
@AllArgsConstructor
class MarksVisualization {

    public static final String OUTPUT_VISUALIZE_MARKS_SOLID = "solid";
    public static final String OUTPUT_VISUALIZE_MARKS_OUTLINE = "outline";
    
    private final MarkCollection marks;
    private final Outputter outputter;
    private final DisplayStack backgroundStack;
    
    public void write() {

        // Cache the creation of colored-marks
        CachedSupplier<ColoredMarksWithDisplayStack,OutputWriteFailedException> cachedMarksWithStack = CachedSupplier.cache(this::createMarksWithStack);

        writeColoredMarks(OUTPUT_VISUALIZE_MARKS_SOLID, Filled::new, cachedMarksWithStack::get);
        writeColoredMarks(OUTPUT_VISUALIZE_MARKS_OUTLINE, Outline::new, cachedMarksWithStack::get);
    }
    
    private ColoredMarksWithDisplayStack createMarksWithStack() throws OutputWriteFailedException {
        try {
            ColorIndex colorIndex = outputter.getSettings().defaultColorIndexFor(marks.size());
            
            return new ColoredMarksWithDisplayStack(
                    new ColoredMarks(marks, colorIndex, new IDGetterIter<Mark>()),
                    backgroundStack);
        } catch (OperationFailedException e) {
            throw new OutputWriteFailedException(e);
        }
    }

    private void writeColoredMarks(
            String outputName,
            Supplier<DrawObject> drawObject,
            ElementSupplier<ColoredMarksWithDisplayStack> marksWithStack) {
        outputter.writerSelective().write(
                outputName,
                () -> createMarksGenerator(drawObject.get()), marksWithStack);
    }
    
    private static MarksGenerator createMarksGenerator(DrawObject drawObject) {
        return new MarksGenerator(drawObject, new IDGetterIter<Overlay>());
    }
}
