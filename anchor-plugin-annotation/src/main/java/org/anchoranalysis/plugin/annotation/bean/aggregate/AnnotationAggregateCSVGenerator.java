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
import org.anchoranalysis.core.text.TypedValue;
import org.anchoranalysis.feature.io.csv.FeatureTableCSVGenerator;
import org.anchoranalysis.io.output.csv.CSVWriter;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;

public class AnnotationAggregateCSVGenerator extends FeatureTableCSVGenerator<List<ImageAnnotation>> {

    public AnnotationAggregateCSVGenerator() {
        super("aggregateAnnotationLabel", Arrays.asList("id", "label"));
    }

    @Override
    protected void writeFeaturesToCSV(
            CSVWriter writer, List<ImageAnnotation> allFeatureResults, List<String> headerNames)
            throws OutputWriteFailedException {
        // We add a header line
        writer.writeHeaders(headerNames);

        for (ImageAnnotation ann : allFeatureResults) {

            List<TypedValue> csvRow = new ArrayList<>();
            addString(csvRow, ann.getIdentifier());
            addString(csvRow, ann.getLabel());
            writer.writeRow(csvRow);
        }
    }

    private static void addString(List<TypedValue> csvRow, String text) {
        csvRow.add(new TypedValue(text, false));
    }
}
