/*-
 * #%L
 * anchor-plugin-io
 * %%
 * Copyright (C) 2010 - 2021 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
package org.anchoranalysis.plugin.io.bean.stack.reader;

import java.nio.file.Path;
import org.anchoranalysis.core.time.ExecutionTimeRecorder;
import org.anchoranalysis.core.time.ExecutionTimeRecorderIgnore;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.stack.reader.StackReader;
import org.anchoranalysis.image.io.bean.stack.reader.StackReaderOrientationCorrection;
import org.anchoranalysis.image.io.stack.CalculateOrientationChange;
import org.anchoranalysis.image.io.stack.input.OpenedImageFile;
import org.anchoranalysis.test.TestLoader;

/**
 * Reads image files from the {@code test/resources}.
 *
 * @author Owen Feehan
 */
class OpenImageFileHelper {

    private final TestLoader loader = TestLoader.createFromMavenWorkingDirectory();

    /** The name of the subdirectory in {@code test/resources} in which to find image-files. */
    private final String subdirectory;

    /** How to read images. */
    private final StackReaderOrientationCorrection reader;

    /**
     * Creates to read from a particular subdirectory with a particular {@link StackReader}.
     *
     * @param subdirectory the name of the subdirectory in {@code test/resources} in which to find
     *     image-files.
     * @param reader the reader to use.
     */
    public OpenImageFileHelper(String subdirectory, StackReaderOrientationCorrection reader) {
        this.subdirectory = subdirectory;
        this.reader = reader;
    }

    /**
     * Opens an image-file from the {@code subdirectory} passed to the constructor.
     *
     * @param filename the name of a file in {@code subdirectory} to open.
     * @param executionTimeRecorder records the times of certain operations.
     * @return the opened file.
     * @throws ImageIOException if the file cannot be opened.
     */
    public OpenedImageFile openFile(String filename, ExecutionTimeRecorder executionTimeRecorder)
            throws ImageIOException {
        return reader.openFile(pathForFile(filename), executionTimeRecorder);
    }

    /**
     * Opens an image-file from the {@code subdirectory} passed to the constructor.
     *
     * @param filename the name of a file in {@code subdirectory} to open.
     * @param orientationCorrection calculates any needed correction applied to the orientation as
     *     the image is loaded.
     * @return the opened file.
     * @throws ImageIOException if the file cannot be opened.
     */
    public OpenedImageFile openFile(
            String filename, CalculateOrientationChange orientationCorrection)
            throws ImageIOException {
        return reader.openFile(
                pathForFile(filename),
                orientationCorrection,
                ExecutionTimeRecorderIgnore.instance());
    }

    /**
     * The path to open a particular filename.
     *
     * @param filename the filename.
     * @return the path.
     */
    private Path pathForFile(String filename) {
        String relativePath = String.format("%s/%s", subdirectory, filename);
        return loader.resolveTestPath(relativePath);
    }
}
