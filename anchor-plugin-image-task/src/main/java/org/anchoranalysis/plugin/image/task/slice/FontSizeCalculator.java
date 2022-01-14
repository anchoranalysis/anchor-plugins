package org.anchoranalysis.plugin.image.task.slice;

import ij.process.ImageProcessor;
import lombok.AllArgsConstructor;
import org.anchoranalysis.spatial.point.Point2d;

/**
 * Calculates how much space a particular font-size uses, and what the optimal font-size is.
 *
 * @author Owen Feehan
 */
@AllArgsConstructor
class FontSizeCalculator {

    /** The minimum font-size, below which we do not fall for a label. */
    private static final int MINIMUM_FONT_SIZE = 12;

    /** A corresponding approximate height in pixels when using that font for a single-line. */
    private static final int MINIMUM_FONT_HEIGHT = 9;

    /** Example text to be used for calculating bounds on the height of text (in general). */
    private static final String TEXT_FOR_BOUNDS_CALCULATION = "aBHAS12a1PP(";

    /** The {@link ImageProcessor} to calculate a font-size for. */
    private final ImageProcessor processor;

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
            SearchMonoticallyIncreasing search =
                    new SearchMonoticallyIncreasing(
                            targetFontHeight,
                            fontSize -> calculateStringHeight(fontSize, processor));
            return search.findOptimalInput(MINIMUM_FONT_SIZE);
        } else {
            // This font is known to correspond to a height of approximately 15
            return MINIMUM_FONT_SIZE;
        }
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
        return processor.getStringBounds(TEXT_FOR_BOUNDS_CALCULATION).getHeight();
    }
}
