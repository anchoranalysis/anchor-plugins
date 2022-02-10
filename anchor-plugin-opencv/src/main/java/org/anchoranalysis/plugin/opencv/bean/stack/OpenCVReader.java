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

import com.google.common.base.CharMatcher;
import java.nio.file.Path;
import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.exception.BeanMisconfiguredException;
import org.anchoranalysis.core.time.ExecutionTimeRecorder;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.stack.reader.StackReader;
import org.anchoranalysis.image.io.stack.input.OpenedImageFile;
import org.anchoranalysis.plugin.io.bean.stack.reader.RotateImageToMatchEXIFOrientation;
import org.anchoranalysis.plugin.opencv.CVInit;

/**
 * Reads a stack from the file-system using OpenCV.
 *
 * <p>Depending on whether the file-path is ASCII-encoded or not-ASCII encoded, this occurs via two
 * different implementations.
 *
 * <p>The non-ASCII method seems to be approximately 4 times slower empirically.
 *
 * <p>See this <a
 * href="https://stackoverflow.com/questions/43185605/how-do-i-read-an-image-from-a-path-with-unicode-characters">Stack
 * Overflow post</a> for more details on the problem/
 *
 * <p>Note that when <i>reading image metadata</i> only, this is computionally slow, as the entire
 * image must be loaded into memory to determine the width and height. Users are recommended to use
 * another library for this purpose.
 *
 * <p>However, unlike many other libraries, OpenCV has the advantage of automatically correcting the
 * orientation (to give correct widths and heights) where EXIF rotation information is present - but
 * only for the ASCII-mathod.
 *
 * @author Owen Feehan
 */
public class OpenCVReader extends StackReader {

    /** The delegate to use for ASCII-encoded paths. */
    private StackReader delegateAscii = new OpenCVReaderAscii();

    /** The delegate to use for non-ASCII-encoded paths. */
    private StackReader delegateNotAscii =
            new RotateImageToMatchEXIFOrientation(new OpenCVReaderNonAscii());

    static {
        CVInit.alwaysExecuteBeforeCallingLibrary();
    }

    @Override
    public void checkMisconfigured(BeanInstanceMap defaultInstances)
            throws BeanMisconfiguredException {
        super.checkMisconfigured(defaultInstances);
        delegateAscii.checkMisconfigured(defaultInstances);
        delegateNotAscii.checkMisconfigured(defaultInstances);
    }

    @Override
    public OpenedImageFile openFile(Path path, ExecutionTimeRecorder executionTimeRecorder)
            throws ImageIOException {
        CVInit.blockUntilLoaded();

        String pathAsString = path.toString();
        boolean isAscii = CharMatcher.ascii().matchesAllOf(pathAsString);
        if (isAscii) {
            return delegateAscii.openFile(path, executionTimeRecorder);
        } else {
            return delegateNotAscii.openFile(path, executionTimeRecorder);
        }
    }
}
