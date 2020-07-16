/* (C)2020 */
package org.anchoranalysis.plugin.annotation.bean.aggregate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.anchoranalysis.core.text.TypedValue;
import org.anchoranalysis.feature.io.csv.writer.TableCSVGenerator;
import org.anchoranalysis.io.output.csv.CSVWriter;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;

public class AnnotationAggregateCSVGenerator extends TableCSVGenerator<List<ImageAnnotation>> {

    public AnnotationAggregateCSVGenerator() {
        super("aggregateAnnotationLabel", Arrays.asList("id", "label"));
    }

    @Override
    protected void writeRowsAndColumns(
            CSVWriter writer, List<ImageAnnotation> rows, List<String> headerNames)
            throws OutputWriteFailedException {
        // We add a header line
        writer.writeHeaders(headerNames);

        for (ImageAnnotation ann : rows) {

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
