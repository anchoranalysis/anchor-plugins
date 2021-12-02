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

package org.anchoranalysis.plugin.opencv.bean.object.segment.stack;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.plugin.opencv.convert.ConvertToMat;
import org.anchoranalysis.spatial.box.Extent;
import org.anchoranalysis.spatial.scale.ScaleFactor;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Creates a scaled-version of a stack to use as input
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class CreateScaledInput {

    /**
     * Returns version of a {@link Stack} that is scaled down in size, but converted into a {@link
     * Mat}.
     *
     * @param stack the stack to scale down in size, which should have three channels in RGB order.
     * @param scaleFactor the scaleFactor to use for scaling-down.
     * @param swapRB if true, the first channel and third channel in {@code stack} are swapped to
     *     make the {@link Mat} to e.g. translate RGB to BGR (as expected by OpenCV).
     * @return a newly created {@link Mat} representing a scaled-down version of {@code stack}.
     * @throws CreateException if the number of channels, or data-type of {@code stack} does not
     *     meet expectations.
     */
    public static Mat apply(Stack stack, ScaleFactor scaleFactor, boolean swapRB)
            throws CreateException {

        Mat originalSize = ConvertToMat.makeRGBStack(stack, swapRB);
        return resizeMatToTarget(originalSize, stack.extent().scaleXYBy(scaleFactor));
    }

    private static Mat resizeMatToTarget(Mat src, Extent targetExtent) {
        Mat destination = new Mat();
        Size size = new Size(targetExtent.x(), targetExtent.y());
        Imgproc.resize(src, destination, size);
        return destination;
    }
}
