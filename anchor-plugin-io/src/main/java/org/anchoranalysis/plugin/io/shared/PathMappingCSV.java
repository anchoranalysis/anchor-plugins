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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.core.value.TypedValue;
import org.anchoranalysis.io.generator.tabular.CSVWriter;
import org.anchoranalysis.io.output.outputter.Outputter;

/**
 * Writes entries into a CSV of path-mappings.
 * 
 * @author Owen Feehan
 *
 */
class PathMappingCSV {

    private static final List<String> CSV_HEADERS = Arrays.asList("index", "source_path", "destination_path", "source_filename", "destination_filename");
    
    private final Optional<CSVWriter> writer;
    
    /**
     * Creates a writer with a particular outputName and outputter.
     * 
     * @param outputName the output-name for writing and checking whether it's enabled.
     * @param outputter the outputter ot use for writing and check whether it's enabled.
     */
    public PathMappingCSV(String outputName, Outputter outputter) {
        this.writer = CSVWriter.createFromOutputterWithHeaders(outputName,
                outputter.getChecked(), () -> CSV_HEADERS, outputter.getErrorReporter());
    }
    
    /**
     * Writes a row to the CSV file if the output has been enabled.
     * 
     * @param source source-path for copying operation
     * @param destination destination-path for copying operation
     * @param index the index of file (an integer number uniquely assigned to each operation)
     */
    public void maybeWriteRow(Path source, Path destination, int index) {
        if (writer.isPresent()) {
            writer.get().writeRow( buildCSVRow(source, destination, index));
        }
    }
    
    private static List<TypedValue> buildCSVRow(Path source, Path destination, int index) {
        return Arrays.asList(
                new TypedValue(index),
                pathValue(source),
                pathValue(destination),
                fileNameValue(source),
                fileNameValue(destination));
    }
    
    private static TypedValue pathValue(Path path) {
        return new TypedValue(path.toString());
    }
    
    private static TypedValue fileNameValue(Path path) {
        return new TypedValue(path.getFileName().toString());
    }
}
