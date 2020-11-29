/*-
 * #%L
 * anchor-plugin-io
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

package org.anchoranalysis.plugin.io.bean.summarizer.path;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.math.arithmetic.RunningSum;
import org.anchoranalysis.plugin.io.bean.summarizer.Summarizer;
import org.apache.commons.io.FileUtils;

/**
 * Tracks the range of file-size. Thead-safe.
 *
 * @author Owen Feehan
 */
public class FileSizeStatistics extends Summarizer<Path> {

    private RunningSum runningSum = new RunningSum();
    private long min = Long.MAX_VALUE;
    private long max = Long.MIN_VALUE;

    @Override
    public void add(Path filePath) throws OperationFailedException {

        try {
            long size = Files.size(filePath);

            synchronized (this) {
                runningSum.increment(size);

                if (size < min) {
                    min = size;
                }

                if (size > max) {
                    max = size;
                }
            }

        } catch (IOException e) {
            throw new OperationFailedException(e);
        }
    }

    // Describes all the extensions found
    @Override
    public synchronized String describe() throws OperationFailedException {

        if (runningSum.getCount() == 0) {
            throw new OperationFailedException("There are no paths to summarize");
        }

        if (runningSum.getCount() == 1) {
            // Special case if all files are the same size
            return String.format("File-size is %s.", humanReadableSize(min));
        }

        if (min == max) {
            // Special case if all files are the same size
            return String.format("All inputs have identical size of %s.", humanReadableSize(min));
        }

        return String.format(
                "file-sizes range across [%s to %s] with an average of %s.",
                humanReadableSize(min),
                humanReadableSize(max),
                humanReadableSize((long) runningSum.mean()));
    }

    private static String humanReadableSize(long fileSizeBytes) {
        return FileUtils.byteCountToDisplaySize(fileSizeBytes);
    }
}
