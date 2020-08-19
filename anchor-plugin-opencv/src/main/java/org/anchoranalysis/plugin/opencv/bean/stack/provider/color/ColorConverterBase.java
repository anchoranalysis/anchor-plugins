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

package org.anchoranalysis.plugin.opencv.bean.stack.provider.color;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.stack.StackProviderUnary;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.plugin.opencv.CVInit;
import org.anchoranalysis.plugin.opencv.MatConverter;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

/**
 * Converts a RGB stack into another color space using OpenCV
 *
 * <p>Note: there might be quite a bit of redundant memory allocation here as the Java ByteArrays
 * aren't directly usable in OpenCV and vice-versa, so new images are created both inwards and
 * outwards.
 *
 * <p>TODO: find a way to use the same allocated memory in both the Java structures and the OpenCV
 * structures
 *
 * @author Owen Feehan
 */
public abstract class ColorConverterBase extends StackProviderUnary {

    static {
        CVInit.alwaysExecuteBeforeCallingLibrary();
    }

    @Override
    public Stack createFromStack(Stack stackRGB) throws CreateException {

        checkNumChnls(stackRGB);

        Mat matBGR = MatConverter.makeRGBStack(stackRGB);

        Mat matHSV = convertColorSpace(stackRGB.extent(), matBGR, colorSpaceCode());

        return createOutputStack(stackRGB, matHSV);
    }

    /**
     * The color space conversion code to use from OpenCV
     *
     * <p>Assume that the inputted image is provided is a 3 channel stack in BGR order
     */
    protected abstract int colorSpaceCode();

    private static Mat convertColorSpace(Extent e, Mat matBGR, int code) {
        Mat matHSV = MatConverter.createEmptyMat(e, CvType.CV_8UC3);
        Imgproc.cvtColor(matBGR, matHSV, code);
        return matHSV;
    }

    private void checkNumChnls(Stack stack) throws CreateException {
        if (stack.getNumberChannels() != 3) {
            throw new CreateException(
                    "Input stack must have exactly 3 channels representing a RGB image");
        }
    }

    private static Stack createOutputStack(Stack stackIn, Mat matOut) throws CreateException {
        Stack stackOut = stackIn.duplicate();
        MatConverter.matToRGB(matOut, stackOut);
        return stackOut;
    }
}
