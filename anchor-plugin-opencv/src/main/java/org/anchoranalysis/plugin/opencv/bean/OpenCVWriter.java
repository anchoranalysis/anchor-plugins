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

package org.anchoranalysis.plugin.opencv.bean;

import java.nio.file.Path;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.rasterwriter.OneOrThreeChannelsWriter;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.plugin.opencv.CVInit;
import org.anchoranalysis.plugin.opencv.convert.ConvertToMat;
import org.opencv.imgcodecs.Imgcodecs;

/**
 * Writes a stack to the filesystem using OpenCV and a specified extension.
 * 
 * <p>Note that as initialization of OpenCV can take many seconds, this writer
 * is not recommended as a default writer for anchor's command-line usage, which
 * may wish to very quickly execute jobs.
 * 
 * @author Owen Feehan
 *
 */
public class OpenCVWriter extends OneOrThreeChannelsWriter {

    static {
        CVInit.alwaysExecuteBeforeCallingLibrary();
    }

    @Override
    protected synchronized void writeStackAfterCheck(Stack stack, Path filePath) throws RasterIOException {
        CVInit.blockUntilLoaded();
        
        try {
            Imgcodecs.imwrite(filePath.toString(), ConvertToMat.fromStack(stack));
        } catch (CreateException e) {
            throw new RasterIOException(e);
        }
    }
}
