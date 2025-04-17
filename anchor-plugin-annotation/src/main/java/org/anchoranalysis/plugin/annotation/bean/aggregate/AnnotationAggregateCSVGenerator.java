/*-
 * #%L
 * anchor-plugin-annotation
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

package org.anchoranalysis.plugin.annotation.bean.aggregate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.anchoranalysis.core.value.TypedValue;
import org.anchoranalysis.feature.io.csv.table.FeatureTableCSVGenerator;
import org.anchoranalysis.io.generator.tabular.CSVWriter;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;

/**
 * Generates a CSV file containing aggregated annotation data.
 *
 * <p>This class extends {@link FeatureTableCSVGenerator} to create a CSV file with columns for image ID and label.
 */
public class AnnotationAggregateCSVGenerator
        extends FeatureTableCSVGenerator<List<ImageAnnotation>> {

    /**
     * Constructs an {@link AnnotationAggregateCSVGenerator} with predefined column headers.
     */
    public AnnotationAggregateCSVGenerator() {
        super(Arrays.asList("id", "label"));
    }

    @Override
    protected void writeFeaturesToCSV(
            CSVWriter writer, List<ImageAnnotation> allFeatureResults, List<String> headerNames)
            throws OutputWriteFailedException {
        // We add a header line
        writer.writeHeaders(headerNames);

        for (ImageAnnotation annotation : allFeatureResults) {

            List<TypedValue> csvRow = new ArrayList<>();
            addString(csvRow, annotation.getIdentifier());
            addString(csvRow, annotation.getLabel());
            writer.writeRow(csvRow);
        }
    }

    /**
     * Adds a string value to the CSV row.
     *
     * @param csvRow the list representing a CSV row
     * @param text the string to be added to the row
     */
    private static void addString(List<TypedValue> csvRow, String text) {
        csvRow.add(new TypedValue(text, false));
    }
}