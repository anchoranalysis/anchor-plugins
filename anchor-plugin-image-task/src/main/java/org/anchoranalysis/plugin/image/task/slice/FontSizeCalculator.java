/*-
 * #%L
 * anchor-plugin-image-task
 * %%
 * Copyright (C) 2010 - 2022 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
package org.anchoranalysis.plugin.image.task.slice;

import ij.process.ImageProcessor;
import java.util.Comparator;
import java.util.stream.Stream;
import org.anchoranalysis.math.optimization.SearchClosestValueMonoticallyIncreasing;
import org.anchoranalysis.spatial.point.Point2d;

/**
 * Calculates how much space a particular font-size uses, and what the optimal font-size is.
 *
 * @author Owen Feehan
 */
class FontSizeCalculator {

    /** The minimum font-size, below which we do not fall for a label. */
    private static final int MINIMUM_FONT_SIZE = 12;

    /** A corresponding approximate height in pixels when using that font for a single-line. */
    private static final int MINIMUM_FONT_HEIGHT = 9;

    /**
     * Example text to be used for calculating bounds on the <b>height</b> of text (in general).
     *
     * <p>The characters are chosen so that some of them have maximal height.
     */
    private static final String TEXT_HEIGHT_BOUNDS = "aBHAS12a1PP(";

    /** Example text to be used for calculating bounds on the <b>width</b> of text (in general). */
    private final LabelToWrite labelWidthBounds;

    /** The {@link ImageProcessor} to calculate a font-size for. */
    private final ImageProcessor processor;

    /**
     * Create for a particular processor and set of labels.
     *
     * @param processor the {@link ImageProcessor} to calculate a font-size for.
     * @param labels all labels to be written, each together with their associated bounding-box. It
     *     must have at least one element.
     */
    public FontSizeCalculator(ImageProcessor processor, Stream<LabelToWrite> labels) {
        this.processor = processor;
        this.labelWidthBounds = calculateLabelWithMaxWidthRatio(labels);
    }

    /**
     * Calculates the optimal font-size to use, to produce labels that have a height that is {@code
     * ratioHeightForLabel} of the average box height.
     *
     * @param averageBoxSize the average size of each bounding-box to which an image is copied.
     * @param ratioHeightForLabel how much of the average box height should the label approximately
     *     be sized to.
     * @return the optimal font-size.
     */
    public int calculateOptimalFontSize(Point2d averageBoxSize, double ratioHeightForLabel) {

        // As an appproximate target size, we wish for our labels to be ratioHeightForLabel of the
        // average row height

        double targetFontHeight = averageBoxSize.y() * ratioHeightForLabel;
        if (targetFontHeight > MINIMUM_FONT_HEIGHT) {
            // As we don't know how much size different fonts will take, we need to measure this
            // using an exponential search algorithm.

            // The second argument calculates the height of the text (with the search trying to
            // bring
            // it close to targetFontHeight.

            // The third argument evaluates to false, so long as the width of the text (of the label
            // we are most conerned about) remains within its bounding-box. If it equals or exceeds
            // it
            // it returns true. This imposes a constraint on the search.

            // The third argument is guaranteed by the implementation to be called only immediately
            // after a call to the second argument. This allows us reuse the font-size on the
            // processor/ without setting it freshly.
            SearchClosestValueMonoticallyIncreasing search =
                    new SearchClosestValueMonoticallyIncreasing(
                            targetFontHeight,
                            fontSize -> calculateStringHeight(fontSize, processor),
                            fontSize ->
                                    calculateStringWidth(processor)
                                            > labelWidthBounds.getBoxImage().extent().x());
            return search.findOptimalInput(MINIMUM_FONT_SIZE);
        } else {
            // This font is known to correspond to a height of approximately 15
            return MINIMUM_FONT_SIZE;
        }
    }

    /**
     * The width in pixels for a row of text on a particular {@link ImageProcessor}.
     *
     * <p>The processor will always use the font-size already set on {@code processor}.
     */
    private int calculateStringWidth(ImageProcessor processor) {
        return processor.getStringWidth(labelWidthBounds.getText());
    }

    /**
     * Determine which label is most likely not to fit in terms of width.
     *
     * <p>It is assumed that this is the label with the maximal number of characters per pixel
     * (although strictly-speaking not all characters are of equal width).
     *
     * @param labels all labels to be written, each together with their associated bounding-box. It
     *     must have at least one element.
     * @return the selected label, that has the maximum number of characters per unit width (of the
     *     bounding-box).
     */
    private static LabelToWrite calculateLabelWithMaxWidthRatio(Stream<LabelToWrite> labels) {
        return labels.max( // NOSONAR
                        Comparator.comparing(LabelToWrite::ratioNumberCharactersToWidth))
                .get();
    }

    /**
     * The height in pixels for a row of text of a particular font-size on a particular {@link
     * ImageProcessor}.
     *
     * <p>It includes any background space (whitespace) between the x drawing coordinate and the
     * string, but not necessarily all whitespace at the right.
     */
    private static double calculateStringHeight(int fontSize, ImageProcessor processor) {
        processor.setFontSize(fontSize);
        return processor.getStringBounds(TEXT_HEIGHT_BOUNDS).getHeight();
    }
}
