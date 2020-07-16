/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.task;

import static org.anchoranalysis.plugin.io.bean.task.TypedValueUtilities.*;

import java.util.ArrayList;
import java.util.List;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.text.TypedValue;
import org.anchoranalysis.io.csv.reader.CSVReaderByLine.ReadByLine;
import org.anchoranalysis.io.csv.reader.CSVReaderException;
import org.anchoranalysis.io.output.csv.CSVWriter;

class AddWithName {

    private CSVWriter writer;
    private boolean firstLineHeaders;
    private String name; // null indicates no-name

    public AddWithName(CSVWriter writer, boolean firstLineHeaders, String name) {
        super();
        this.writer = writer;
        this.firstLineHeaders = firstLineHeaders;
        this.name = name;
    }

    public void addNonTransposed(ReadByLine readByLine) throws CSVReaderException {
        readByLine.read(this::addNonTransposedLine);
    }

    public void addTransposed(ReadByLine readByLine) throws CSVReaderException {

        List<String[]> rows = readRowsFromCSV(readByLine);

        if (rows.isEmpty()) {
            return;
        }

        int numCols = rows.get(0).length;
        int firstCol = 0;

        try {
            // If we haven't written the headers get them
            if (firstLineHeaders) {
                if (!writer.hasWrittenHeaders()) {
                    List<String> headers = createArrayFromList(rows, 0);
                    writeHeaders(headers);
                }
                firstCol = 1;
            }

            for (int c = firstCol; c < numCols; c++) {
                List<TypedValue> colData = createTypedArrayFromList(rows, c);
                writeRow(colData);
            }

        } catch (OperationFailedException e) {
            throw new CSVReaderException(e);
        }
    }

    private void addNonTransposedLine(String[] line, boolean firstLine)
            throws OperationFailedException {
        if (firstLine && firstLineHeaders) {
            if (!writer.hasWrittenHeaders()) {
                writeHeaders(listFromArray(line));
            }
            return;
        }

        writeRow(typeArray(line));
    }

    // Modifies the list
    private void writeHeaders(List<String> list) {
        if (name != null) {
            list.add(0, "name");
        }
        writer.writeHeaders(list);
    }

    // Modifies the list
    private void writeRow(List<TypedValue> list) {
        if (name != null) {
            list.add(0, new TypedValue(name));
        }
        writer.writeRow(list);
    }

    private static List<String[]> readRowsFromCSV(ReadByLine readByLine) throws CSVReaderException {
        List<String[]> rows = new ArrayList<>();

        readByLine.read((line, firstLine) -> rows.add(line));

        return rows;
    }
}
