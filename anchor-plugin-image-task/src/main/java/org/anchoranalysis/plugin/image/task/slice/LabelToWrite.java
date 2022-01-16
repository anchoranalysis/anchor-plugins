package org.anchoranalysis.plugin.image.task.slice;

import ij.process.ImageProcessor;
import java.awt.Color;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.bean.spatial.arrange.align.BoxAligner;
import org.anchoranalysis.spatial.box.BoundingBox;
import org.anchoranalysis.spatial.box.Extent;

/** A label to be written on an image. */
@AllArgsConstructor
class LabelToWrite {

    /** The text of the label to write. */
    @Getter private String text;

    /**
     * The entire bounding-box associated with the image that is being written, with which {@code
     * label} is associated.
     */
    @Getter private BoundingBox box;

    /**
     * Draws the label on an {@link ImageProcessor}.
     *
     * @param processor the processor.
     * @param background fill color for the background behind the text.
     * @param aligner how to align the label on its respective associated image.
     * @throws OperationFailedException if the alignment of the text fails.
     */
    public void drawOnProcessor(ImageProcessor processor, Color background, BoxAligner aligner)
            throws OperationFailedException {
        // Determine how many pixels will the string occupy. This is unfortunately only approximate.

        Extent textSize = calculateTextSize(processor).minimum(box.extent());

        BoundingBox textBox = aligner.align(textSize, box);

        processor.drawString(
                text,
                textBox.cornerMin().x(),
                textBox.calculateCornerMaxExclusive().y(),
                background);
    }

    /**
     * The ratio of the number of characters in {@code text} to the width of {@code box}
     *
     * @return the ratio.
     */
    public double ratioNumberCharactersToWidth() {
        return ((double) text.length()) / box.extent().x();
    }

    /**
     * Calculates the number of pixels the text occupies on the screen approximately, across both X
     * and Y dimensions.
     */
    private Extent calculateTextSize(ImageProcessor processor) {
        int width = processor.getStringWidth(text);
        int height = (int) Math.ceil(processor.getStringBounds(text).getHeight());
        return new Extent(width, height);
    }
}
