/*-
 * #%L
 * anchor-plugin-image-task
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

package org.anchoranalysis.plugin.image.task.stack;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.value.TypedValue;
import org.anchoranalysis.feature.io.csv.FeatureCSVWriter;
import org.anchoranalysis.feature.io.csv.metadata.FeatureCSVMetadata;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.outputter.Outputter;

/** Shared state for writing selected slice information to a CSV file. */
public class SharedStateSelectedSlice {

    /** The CSV writer for outputting selected slice information. */
    private final Optional<FeatureCSVWriter> csvWriter;

    /**
     * Creates a new instance of SharedStateSelectedSlice.
     *
     * @param baseOutputter the base outputter for creating the CSV writer
     * @throws CreateException if there's an error creating the CSV writer
     */
    public SharedStateSelectedSlice(Outputter baseOutputter) throws CreateException {
        try {
            this.csvWriter =
                    FeatureCSVWriter.create(
                            new FeatureCSVMetadata(
                                    "selectedSlices",
                                    Arrays.asList("name", "sliceIndex", "featureOptima")),
                            baseOutputter,
                            false);
        } catch (OutputWriteFailedException e) {
            throw new CreateException(e);
        }
    }

    /**
     * Writes a row of selected slice information to the CSV file.
     *
     * @param name the name of the image or stack
     * @param selectedSliceIndex the index of the selected slice
     * @param featureOptima the feature optima value
     */
    public synchronized void writeRow(String name, int selectedSliceIndex, double featureOptima) {
        List<TypedValue> row =
                Arrays.asList(
                        new TypedValue(name),
                        new TypedValue(selectedSliceIndex),
                        new TypedValue(featureOptima, 7));
        this.csvWriter.ifPresent(writer -> writer.addRow(row));
    }

    /** Closes the CSV writer if it exists. */
    public void close() {
        csvWriter.ifPresent(FeatureCSVWriter::close);
    }
}
