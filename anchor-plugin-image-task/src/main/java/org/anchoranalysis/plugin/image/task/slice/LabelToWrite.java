package org.anchoranalysis.plugin.image.task.slice;

import ij.process.ImageProcessor;
import java.awt.Color;
import lombok.AllArgsConstructor;
import org.anchoranalysis.spatial.box.BoundingBox;

/** A label to be written on an image. */
@AllArgsConstructor
class LabelToWrite {

    /** Number of pixels whitespace margin from <b>left</b>-border before writing the label. */
    private static final int MARGIN_LEFT = 0;

    /** Number of pixels whitespace margin from <b>bottom</b>-border before writing the label. */
    private static final int MARGIN_BOTTOM = 0;

    /** The text of the label to write. */
    private String text;

    /**
     * The entire bounding-box associated with the image that is being written, with which {@code
     * label} is associated.
     */
    private BoundingBox box;

    /**
     * Draws the label on an {@link ImageProcessor}.
     *
     * @param processor the processor.
     * @param background fill color for the background behind the text.
     */
    public void drawOnProcessor(ImageProcessor processor, Color background) {
        processor.drawString(
                text,
                box.cornerMin().x() + MARGIN_LEFT,
                box.calculateCornerMaxInclusive().y() - MARGIN_BOTTOM,
                background);
    }
}
