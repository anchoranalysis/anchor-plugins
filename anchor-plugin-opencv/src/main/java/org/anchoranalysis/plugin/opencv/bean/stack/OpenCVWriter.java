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

package org.anchoranalysis.plugin.opencv.bean.stack;

import java.nio.file.Path;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.stack.writer.OneOrThreeChannelsWriter;
import org.anchoranalysis.image.io.bean.stack.writer.WriterErrorMessageHelper;
import org.anchoranalysis.plugin.opencv.CVInit;
import org.anchoranalysis.plugin.opencv.convert.ConvertToMat;
import org.opencv.imgcodecs.Imgcodecs;

/**
 * Writes a stack to the file-system using OpenCV and a specified extension.
 *
 * @author Owen Feehan
 */
public class OpenCVWriter extends OneOrThreeChannelsWriter {

    static {
        CVInit.alwaysExecuteBeforeCallingLibrary();
    }

    @Override
    protected void writeStackAfterCheck(Stack stack, Path filePath)
            throws ImageIOException {
        CVInit.blockUntilLoaded();

        try {
            boolean success = Imgcodecs.imwrite(filePath.toString(), ConvertToMat.fromStack(stack));
            if (!success) {
                throw new ImageIOException("OpenCV's imwrite failed to write: " + filePath);
            }
        } catch (CreateException e) {
            throw WriterErrorMessageHelper.generalWriteException(OpenCVWriter.class, filePath, e);
        }
    }
}
