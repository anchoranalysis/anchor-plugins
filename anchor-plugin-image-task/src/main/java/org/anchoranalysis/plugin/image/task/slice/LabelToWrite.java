package org.anchoranalysis.plugin.image.task.slice;

import ij.process.ImageProcessor;
import java.awt.Color;
import lombok.Getter;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.bean.spatial.arrange.align.BoxAligner;
import org.anchoranalysis.spatial.box.BoundingBox;
import org.anchoranalysis.spatial.box.Extent;

/** A label to be written on an image. */
class LabelToWrite {

    /** The text of the label to write, including any error suffix string when applicable. */
    @Getter private String text;

    /**
     * The entire bounding-box associated with the image that is being written, with which {@code
     * label} is associated.
     */
    @Getter private BoundingBox box;

    /** Whether an error occurred copying the image corresponding to this label. */
    private boolean errored;

    /**
     * Create to write text at a particular {@link BoundingBox}.
     *
     * @param text the text of the label to write, without any suffix if errored. This will be
     *     appended in the constructor when {@code errored} is true.
     * @param box the entire bounding-box associated with the image that is being written, with
     *     which {@code label} is associated.
     * @param errored whether an error occurred copying the image corresponding to this label.
     */
    public LabelToWrite(String text, BoundingBox box, boolean errored) {
        this.box = box;
        this.errored = errored;
        if (errored) {
            this.text = text + " (errored)";
        } else {
            this.text = text;
        }
    }

    /**
     * Draws the label on an {@link ImageProcessor}.
     *
     * @param processor the processor.
     * @param backgroundSuccessful fill color for the background behind the text, when the copying
     *     was <b>successful</b>.
     * @param backgroundErrored fill color for the background behind the text, when the copying was
     *     <b>errored</b>.
     * @param aligner how to align the label on its respective associated image.
     * @throws OperationFailedException if the alignment of the text fails.
     */
    public void drawOnProcessor(
            ImageProcessor processor,
            Color backgroundSuccessful,
            Color backgroundErrored,
            BoxAligner aligner)
            throws OperationFailedException {
        // Determine how many pixels will the string occupy. This is unfortunately only approximate.

        Extent textSize = calculateTextSize(processor).minimum(box.extent());

        BoundingBox textBox = aligner.align(textSize, box);

        processor.drawString(
                text,
                textBox.cornerMin().x(),
                textBox.calculateCornerMaxExclusive().y(),
                errored ? backgroundErrored : backgroundSuccessful);
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
