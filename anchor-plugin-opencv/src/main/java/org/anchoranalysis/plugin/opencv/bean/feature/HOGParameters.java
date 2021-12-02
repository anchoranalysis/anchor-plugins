/*-
 * #%L
 * anchor-plugin-opencv
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

package org.anchoranalysis.plugin.opencv.bean.feature;

import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.image.bean.spatial.SizeXY;
import org.anchoranalysis.spatial.box.Extent;
import org.opencv.core.Size;
import org.opencv.objdetect.HOGDescriptor;

/**
 * Parameters for calculating a HOG Descriptor covering window-size, block-size etc.
 *
 * @author Owen Feehan
 */
@EqualsAndHashCode(callSuper = false)
public class HOGParameters extends AnchorBean<HOGParameters> {

    private static final String BLOCK_SIZE = "block-size";
    private static final String BLOCK_STRIDE = "block-stride";
    private static final String IMAGE_SIZE = "image-size";
    private static final String CELL_SIZE = "cell-size";

    // START BEAN PROPERTIES
    /**
     * The window-size as per OpenCV implementation. If not specified, the window is set to be the
     * same size as the (possibly resized) image.
     */
    @BeanField @OptionalBean @Getter @Setter private SizeXY windowSize;

    /** The block-size as per OpenCV implementation (identical default-size). */
    @BeanField @Getter @Setter private SizeXY blockSize = new SizeXY(16, 16);

    /** The block-stride as per OpenCV implementation (identical default-size). */
    @BeanField @Getter @Setter private SizeXY blockStride = new SizeXY(8, 8);

    /** The cell-size as per OpenCV implementation (identical default-size). */
    @BeanField @Getter @Setter private SizeXY cellSize = new SizeXY(8, 8);

    /** The number of bins in each histogram for a cell. */
    @BeanField @Getter @Setter private int numberBins = 9;
    // END BEAN PROPERTIES

    /**
     * Calculates the size of the descriptor will be for a given image.
     *
     * <p>It assumes the window is equal to the image-size.
     *
     * @param imageSize the size of the image (and window).
     * @return the size of the descriptor needed.
     */
    public int sizeDescriptor(Extent imageSize) {

        Size sizeOfWindow = determineWindowSize(imageSize);
        // windowStride defaults to cellSize if Size() is passed (see OpenCV source file hog.cpp)
        SizeXY windowStride = cellSize;

        int cellsPerBlock =
                DivideUtilities.divide(blockSize, cellSize, SizeXY::getWidth)
                        * DivideUtilities.divide(blockSize, cellSize, SizeXY::getHeight);

        int blocksPerWindow =
                numberSlidingWindowsFor(sizeOfWindow, sizeFor(blockSize), cellSize, blockStride);

        int windowsPerImage =
                numberSlidingWindowsFor(sizeFor(imageSize), sizeOfWindow, cellSize, windowStride);

        // We assume we are always using the default winStride, which is equal to cellSize
        // windows per image
        return blocksPerWindow * cellsPerBlock * numberBins * windowsPerImage;
    }

    /**
     * Creates an empty {@link HOGDescriptor} to describe an image of a particular size.
     *
     * @param imageSize the size of the image.
     * @return the descriptor, not yet computed with meaningful values.
     */
    public HOGDescriptor createDescriptor(Extent imageSize) {
        return new HOGDescriptor(
                determineWindowSize(imageSize),
                sizeFor(blockSize),
                sizeFor(blockStride),
                sizeFor(cellSize),
                numberBins);
    }

    /**
     * Throws an exception if an image of size {@code extent} cannot be described by these
     * parameters.
     *
     * @param extent the size of the image to check.
     * @throws FeatureCalculationException if the image is inappropriately sized for these
     *     parameters.
     */
    public void checkSize(Extent extent) throws FeatureCalculationException {

        SizeXY extentAsSize = new SizeXY(extent);

        DivideUtilities.checkDivisibleBy(extentAsSize, blockSize, IMAGE_SIZE, BLOCK_SIZE);
        DivideUtilities.checkDivisibleBy(blockSize, cellSize, BLOCK_SIZE, CELL_SIZE);
        DivideUtilities.checkDivisibleBy(blockSize, blockStride, BLOCK_SIZE, BLOCK_STRIDE);
        DivideUtilities.checkDivisibleBy(blockStride, cellSize, BLOCK_STRIDE, CELL_SIZE);

        if (windowSize != null) {
            if (extent.x() < windowSize.getWidth()) {
                throw new FeatureCalculationException(
                        "Image width is smaller than HOG window width. This is not permitted.");
            }
            if (extent.y() < windowSize.getHeight()) {
                throw new FeatureCalculationException(
                        "Image height is smaller than HOG window height. This is not permitted.");
            }
        }
    }

    private Size determineWindowSize(Extent imageSize) {
        return convertOr(Optional.ofNullable(windowSize), imageSize);
    }

    private static int numberSlidingWindowsFor(
            Size size, Size subtractFromSize, SizeXY addToSize, SizeXY stride) {

        int timesX =
                DivideUtilities.ceilDiv(
                        size.width - subtractFromSize.width + addToSize.getWidth(),
                        stride.getWidth());

        int timesY =
                DivideUtilities.ceilDiv(
                        size.height - subtractFromSize.height + addToSize.getHeight(),
                        stride.getHeight());

        return timesX * timesY;
    }

    /** Convert to OpenCV Size class. */
    private static Size convertOr(Optional<SizeXY> in, Extent imageSize) {
        return in.map(HOGParameters::sizeFor)
                .orElseGet(() -> new Size(imageSize.x(), imageSize.y()));
    }

    private static Size sizeFor(Extent extent) {
        return new Size(extent.x(), extent.y());
    }

    private static Size sizeFor(SizeXY size) {
        return new Size(size.getWidth(), size.getHeight());
    }
}
