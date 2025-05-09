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
import java.util.LinkedList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.anchoranalysis.core.color.RGBColor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.time.ExecutionTimeRecorder;
import org.anchoranalysis.image.bean.spatial.arrange.align.BoxAligner;
import org.anchoranalysis.image.core.stack.RGBStack;
import org.anchoranalysis.io.imagej.convert.ConvertToImageProcessor;
import org.anchoranalysis.io.imagej.convert.ImageJConversionException;
import org.anchoranalysis.spatial.box.BoundingBox;
import org.anchoranalysis.spatial.point.Point2d;

/**
 * Facilitates a delayed drawing of <i>all</i> text labels on an image, by collecting the labels as
 * images are added.
 *
 * <p>The drawing of the labels take place via ImageJ.
 *
 * <p>This is useful, for two reasons:
 *
 * <ul>
 *   <li>to avoid repeated redundant computational effort create the needed ImageJ classes, each
 *       time that a label is written.
 *   <li>to be aware of the maximum width of the identifiers, to help sensibly pick a label size.
 * </ul>
 *
 * <p>It is
 *
 * @author Owen Feehan
 */
@RequiredArgsConstructor
class MontageLabels {

    private static final RGBColor TEXT_COLOR = new RGBColor(255, 255, 0);

    private static final RGBColor FILL_COLOR_SUCCESSFUL = new RGBColor(0, 0, 255);

    private static final RGBColor FILL_COLOR_ERRORED = new RGBColor(255, 0, 0);

    // START REQUIRED ARGUMENTS
    /** Records the execution time of certain operations. */
    private final ExecutionTimeRecorder executionTimeRecorder;

    // END REQUIRED ARGUMENTS

    /** A running sum of the x- and y- sizes of the queued bounding-boxes. */
    private Point2d sumSizes = new Point2d();

    /** The labels to be written. */
    private List<LabelToWrite> labels = new LinkedList<>();

    /**
     * Adds a label to draw to the queue.
     *
     * @param text the label to draw.
     * @param box the location to draw it at.
     * @param errored whether an error occurred copying the image corresponding to this label.
     */
    public synchronized void add(String text, BoundingBox box, boolean errored) {
        labels.add(new LabelToWrite(text, box, errored));
        sumSizes.add(box.extent().x(), box.extent().y());
    }

    /**
     * Draw all the labels onto the image.
     *
     * <p>Once called, this class is no longer usable, and no subsequent methods should be called.
     *
     * @param stack the image to draw onto.
     * @param ratioHeightForLabel how much of the average box height should the label approximately
     *     be sized to.
     * @param aligner how to align the label on its respective associated image.
     * @throws OperationFailedException if {@code stack} cannot be converted into an {@link
     *     ImageProcessor}.
     */
    public void flush(RGBStack stack, double ratioHeightForLabel, BoxAligner aligner)
            throws OperationFailedException {
        int fontSize = 0;
        try {
            // Iterate through each of the RGB channels respectively, and draw the text in the
            // appropriate color for that channel only.
            //
            // We avoid using a ColorProcessor from ImageJ as this would involve converting from 3x1
            // channel to 1xRGBA channel, and back again
            // incurring computational cost for a (possibly) quite large combined image.
            for (int channel = 0; channel < 3; channel++) {
                ImageProcessor processor =
                        ConvertToImageProcessor.from(stack.getChannel(channel).voxels(), 0);

                // We calculate the font-size only on the processor for the first channel, otherwise
                // it is identical. We need the processor to estimate the font size
                // We only bother if we are written labels
                if (!labels.isEmpty()) {
                    if (channel == 0 && !labels.isEmpty()) {
                        fontSize = calculateFontSize(processor, ratioHeightForLabel);
                    }

                    processor.setJustification(ImageProcessor.LEFT_JUSTIFY);
                    processor.setColor(extractColorComponent(TEXT_COLOR, channel));
                    processor.setFontSize(fontSize);
                }

                Color backgroundSuccessful = extractColorComponent(FILL_COLOR_SUCCESSFUL, channel);
                Color backgroundErrored = extractColorComponent(FILL_COLOR_ERRORED, channel);

                for (LabelToWrite label : labels) {
                    executionTimeRecorder.recordExecutionTime(
                            "Writing the label on a single image channel",
                            () ->
                                    label.drawOnProcessor(
                                            processor,
                                            backgroundSuccessful,
                                            backgroundErrored,
                                            aligner));
                }
            }
            labels = null;
        } catch (ImageJConversionException e) {
            throw new OperationFailedException(e);
        }
    }

    /** Calculates the optimal font-size to use for the label. */
    private int calculateFontSize(ImageProcessor processor, double ratioHeightForLabel) {
        // Converts sumSizes into the average size in each dimension
        sumSizes.scale(1.0 / labels.size());
        FontSizeCalculator calculator = new FontSizeCalculator(processor, labels.stream());
        return calculator.calculateOptimalFontSize(sumSizes, ratioHeightForLabel);
    }

    /**
     * Uses the component from only one part (R, G or B) of {@code color} for all parts of a newly
     * created {@link Color}.
     */
    private static Color extractColorComponent(RGBColor color, int index) {
        int component = color.get(index);
        return new Color(component, component, component);
    }
}
