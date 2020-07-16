/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.summarizer.path;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.anchoranalysis.core.arithmetic.RunningSum;
import org.anchoranalysis.core.error.OperationFailedException;
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
