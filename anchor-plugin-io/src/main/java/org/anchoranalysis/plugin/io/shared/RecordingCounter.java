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
package org.anchoranalysis.plugin.io.shared;

import java.nio.file.Path;
import java.util.Optional;
import org.anchoranalysis.experiment.log.StatefulMessageLogger;
import org.anchoranalysis.experiment.log.TextFileMessageLogger;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.math.arithmetic.Counter;
import org.anchoranalysis.plugin.io.bean.copyfilesmode.naming.CopyFilesNaming;
import lombok.Getter;

/**
 * A counter that also records a manifest of copied outputs in a message-log and CSV file.
 * 
 * <p>The following outputs are produced:
 *
 * <table>
 * <caption></caption>
 * <thead>
 * <tr><th>Output Name</th><th>Default?</th><th>Description</th></tr>
 * </thead>
 * <tbody>
 * <tr><td>copy_log</td><td>no</td><td>a text log file showing each copy operation (and any skipped files).</td></tr>
 * <tr><td>path_mapping</td><td>no</td><td>a CSV file showing the source and destination paths for each copy operation (skipped files are not included)..</td></tr>
 * </tbody>
 * </table>
 * 
 * @param <T> shared-state of {@link CopyFilesNaming}
 * @author Owen Feehan
  */
public class RecordingCounter<T> {

    /**
     * A particular text log-file (placed in the experiment's output directory) indicating whether an input was copied/skipped and if so, the corresponding destination path.
     * 
     * <p>Note that this output can be enabled with {@code OUTPUT_COPY} disabled, to produce a "dummy mode" where files
     * aren't copied, but a log-file is created as if they were.
     */
    private static final String OUTPUT_COPY_LOG = "copy_log";
    
    /**
     * A CSV file of paths and filenames and index for each source and destination file copied.
     */
    private static final String OUTPUT_PATH_MAPPING = "path_mapping";
    
    /** The counter. */
    private final Counter counter;
    
    /** A message log that may or may not be enabled. */
    private final StatefulMessageLogger logger;
    
    /** A mapping of paths. */
    private final PathMappingCSV pathMapping;
    
    @Getter private final T namingSharedState;
    
    public RecordingCounter(Counter counter, Outputter outputter, T namingSharedState) {
        this.counter = counter;
        this.namingSharedState = namingSharedState;
        
        this.logger = new TextFileMessageLogger(OUTPUT_COPY_LOG, outputter.getChecked(), outputter.getErrorReporter());
        this.logger.start();

        this.pathMapping = new PathMappingCSV(OUTPUT_PATH_MAPPING, outputter);
    }
    
    /**
     * Increments the counter.
     * 
     * <p>This is <b>thread-safe</b>.
     * 
     * @return the index of the counter <b>before</b> being incremented.
     */
    public synchronized int incrementCounter() {
        int index = counter.getCount();
        counter.increment();
        return index;
    }
    
    /**
     * Records an copy operation in the log / CSV path mapping.
     * 
     * @param source source-path for copying operation
     * @param destination destination-path for copying operation
     * @param index the index of file (an integer number uniquely assigned to each operation)
     */
    public void recordCopiedOutput(Path source, Optional<Path> destination, int index) {
        if (destination.isPresent()) {
            logger.logFormatted("Copying %s to %s", source, destination.get().toString());
            pathMapping.maybeWriteRow(source, destination.get(), index);
        } else {
            logger.logFormatted("Skipping %s", source);
        }
    }
        
    /**
     * Closes the logger.
     */
    public void closeLogger() {
        logger.close(true);
    }
}
