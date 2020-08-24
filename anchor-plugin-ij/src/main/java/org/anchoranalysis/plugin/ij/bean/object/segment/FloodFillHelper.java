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

package org.anchoranalysis.plugin.ij.bean.object.segment;

import ij.process.ImageProcessor;
import lombok.AllArgsConstructor;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.convert.ByteConverter;

@AllArgsConstructor
class FloodFillHelper {

    private final int minNumberPixels;

    private final int equalValueAsInt;

    private final ImageProcessor processor;

    private final IJFloodFiller floodFiller;

    /**
     * Constructor
     *
     * @param minNumberPixels Minimum number of pixels in a flood-fill otherwise the color is
     *     ignored (set to 0)
     * @param equalValueAsByte all pixels who are flood filled must be equal to this value, and it
     *     is not used as a valid color.
     */
    public FloodFillHelper(int minNumberPixels, byte equalValueAsByte, ImageProcessor processor) {
        this.minNumberPixels = minNumberPixels;
        this.equalValueAsInt = ByteConverter.unsignedByteToInt(equalValueAsByte);
        this.processor = processor;
        this.floodFiller = new IJFloodFiller(processor);
    }

    // works on a single plane, returns highest color value assigned
    // posVal is a value defining 'positive' indicating what will get filled
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
     * Flood fills from a particular point
     *
     * @param x x-value of point
     * @param y y-value of point
     * @param color color to use
     * @return the last color used for filling
     * @throws OperationFailedException
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
