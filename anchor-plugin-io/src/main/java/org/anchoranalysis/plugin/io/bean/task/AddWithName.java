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

package org.anchoranalysis.plugin.io.bean.task;

import static org.anchoranalysis.plugin.io.bean.task.TypedValueUtilities.*;

import java.util.ArrayList;
import java.util.List;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.text.TypedValue;
import org.anchoranalysis.io.csv.reader.CSVReaderByLine.ReadByLine;
import org.anchoranalysis.io.generator.tabular.CSVWriter;
import org.anchoranalysis.io.csv.reader.CSVReaderException;

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
