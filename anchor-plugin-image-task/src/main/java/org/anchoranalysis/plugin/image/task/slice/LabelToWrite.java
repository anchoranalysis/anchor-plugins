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
import java.awt.Color;
import java.awt.Rectangle;
import lombok.Getter;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.bean.spatial.arrange.align.BoxAligner;
import org.anchoranalysis.spatial.box.BoundingBox;
import org.anchoranalysis.spatial.box.Extent;

/** A label to be written on an image. */
class LabelToWrite {

    /** We insist the text label has at least 16 pixels (8 on either side) free from the borders. */
    private static final int MARGIN_PIXELS = 16;

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

        Extent textSize = calculateTextSize(text, processor);

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
        Rectangle bounds = processor.getStringBounds(text);
        int width = (int) Math.ceil(bounds.getWidth());
        int height = (int) Math.ceil(bounds.getHeight());
        return new Extent(width, height);
    }
}
