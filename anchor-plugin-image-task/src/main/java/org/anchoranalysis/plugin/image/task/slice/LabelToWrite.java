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

    /** We insist the text label has at least 6 pixels (3 on either side) free from the borders. */
    private static final int MARGIN_PIXELS = 6;

    /** The text of the label to write, including any error suffix string when applicable. */
    @Getter private String text;

    /**
     * The entire bounding-box associated with the image that is being written, with which {@code
     * label} is associated.
     */
    @Getter private BoundingBox boxImage;

    /** Where to locate the text. */
    private BoundingBox boxText;

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
        this.boxImage = box;
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

        BoundingBox textBox = calculateTextPosition(aligner, processor);

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
        return ((double) text.length()) / boxImage.extent().x();
    }

    /** Calculates where to locate the text, caching it for future calls to other channels. */
    private BoundingBox calculateTextPosition(BoxAligner aligner, ImageProcessor processor)
            throws OperationFailedException {
        if (boxText == null) {
            Extent textSize = calculateSizeMaybeReduceText(processor);
            boxText = aligner.align(textSize, boxImage);
        }
        return boxText;
    }

    /**
     * Calculates the size of the text, removing characters if necessary to let it fit in
     * comfortable.
     *
     * <p>When characters are removed, three dots are suffixed as an indication.
     *
     * <p>i.e. {@code too_big_label_for_image} might become {@code too_big_lab...} on different
     * channels.
     *
     * @param processor the processor that is being draw upon.
     * @return the size of the text.
     */
    private Extent calculateSizeMaybeReduceText(ImageProcessor processor) {

        Extent textSize = calculateTextSize(text, processor).minimum(boxImage.extent());

        // Maximum width allowed
        int maximumPermittedWidth = Math.max(boxImage.extent().x() - MARGIN_PIXELS, MARGIN_PIXELS);

        // Reduce the label by a certain fraction of the characters (which are not all equal in
        // width but
        // this is an approximation).

        if (textSize.x() > maximumPermittedWidth) {
            double fractionToKeep = ((double) maximumPermittedWidth / textSize.x());
            int numberCharactersToKeep =
                    Math.max((int) Math.floor(text.length() * fractionToKeep) - 3, 0);
            // We add three dots at the end to suggest the label has been shorted
            this.text = text.substring(0, numberCharactersToKeep) + "...";
            return calculateTextSize(text, processor);
        } else {
            return textSize;
        }
    }

    /**
     * Calculates the number of pixels the text occupies on the screen approximately, across both X
     * and Y dimensions.
     */
    private static Extent calculateTextSize(String text, ImageProcessor processor) {
        int width = processor.getStringWidth(text);
        int height = (int) Math.ceil(processor.getStringBounds(text).getHeight());
        return new Extent(width, height);
    }
}
