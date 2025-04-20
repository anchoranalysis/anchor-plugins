/*-
 * #%L
 * anchor-plugin-ij
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

package org.anchoranalysis.plugin.imagej.bean.object.segment;

import ij.process.ImageProcessor;
import lombok.AllArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.voxel.buffer.primitive.PrimitiveConverter;

/**
 * Helper class for performing flood fill operations on an {@link ImageProcessor}.
 *
 * <p>This class uses ImageJ's flood fill implementation to segment objects in a 2D image.
 */
@AllArgsConstructor
class FloodFillHelper {

    /** Minimum number of pixels in a flood-fill region to be considered valid. */
    private final int minNumberPixels;

    /** The integer value representing the pixels to be flood filled. */
    private final int equalValueAsInt;

    /** The {@link ImageProcessor} on which flood fill operations are performed. */
    private final ImageProcessor processor;

    /** The {@link IJFloodFiller} used for flood fill operations. */
    private final IJFloodFiller floodFiller;

    /**
     * Constructs a new FloodFillHelper.
     *
     * @param minNumberPixels Minimum number of pixels in a flood-fill otherwise the color is
     *     ignored (set to 0)
     * @param equalValueAsByte all pixels who are flood filled must be equal to this value, and it
     *     is not used as a valid color
     * @param processor the {@link ImageProcessor} to perform flood fill on
     */
    public FloodFillHelper(int minNumberPixels, byte equalValueAsByte, ImageProcessor processor) {
        this.minNumberPixels = minNumberPixels;
        this.equalValueAsInt = PrimitiveConverter.unsignedByteToInt(equalValueAsByte);
        this.processor = processor;
        this.floodFiller = new IJFloodFiller(processor);
    }

    /**
     * Performs flood fill on a single 2D plane.
     *
     * @param startingColor the initial color value to use for flood filling
     * @return the highest color value assigned during flood fill
     * @throws OperationFailedException if more than 254 objects are detected
     */
    public int floodFill2D(int startingColor) throws OperationFailedException {

        // Color, we use colors other than our posval at a posVal
        int color = startingColor - 1;
        for (int y = 0; y < processor.getHeight(); y++) {
            for (int x = 0; x < processor.getWidth(); x++) {
                if (processor.getPixel(x, y) == equalValueAsInt) {
                    color = floodFillFromPoint(x, y, color);
                }
            }
        }
        return color;
    }

    /**
     * Flood fills from a particular point.
     *
     * @param x x-coordinate of the starting point
     * @param y y-coordinate of the starting point
     * @param color color to use for flood fill
     * @return the last color used for filling
     * @throws OperationFailedException if more than 254 objects are detected
     */
    private int floodFillFromPoint(int x, int y, int color) throws OperationFailedException {

        // TODO why this condition?
        if (color != equalValueAsInt) {
            color++;
        }

        if (color == 255) {
            throw new OperationFailedException("More objects that colors (max of 254 allowed)");
        }

        processor.setColor(color);

        int numberFilledPixels = floodFiller.fill(x, y);
        if (numberFilledPixels < minNumberPixels) {
            // Change coloring to zero
            processor.setColor(0);
            floodFiller.fill(x, y);
            color--;
        }
        return color;
    }
}
