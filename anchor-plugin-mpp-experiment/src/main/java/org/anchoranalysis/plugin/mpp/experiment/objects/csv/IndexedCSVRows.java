/*-
 * #%L
 * anchor-plugin-mpp-experiment
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

package org.anchoranalysis.plugin.mpp.experiment.objects.csv;

import java.util.Set;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.io.csv.reader.CSVReaderByLine.ReadByLine;
import org.anchoranalysis.io.csv.reader.CSVReaderException;
import org.anchoranalysis.plugin.mpp.experiment.bean.objects.columndefinition.ColumnDefinition;

/**
 * Stores the rows from the CSV file. An index is kept for both their fileID and their group
 *
 * @author Owen Feehan
 */
public class IndexedCSVRows {

    private MapFileID map = new MapFileID();

    /**
     * Creates an index of the rows of the CSV file according to a particular columnDefinition
     *
     * @param csvFile the csvFile (with no lines yet read)
     * @param columnDefinition defines the name of columns in the CSV file
     * @throws CreateException
     */
    public IndexedCSVRows(ReadByLine csvFile, ColumnDefinition columnDefinition)
            throws CreateException {
        assert (columnDefinition != null);

        try {
            String[] headers = csvFile.headers();
            columnDefinition.initHeaders(headers);

            processLines(csvFile, columnDefinition);
        } catch (CSVReaderException | InitException e) {
            throw new CreateException(e);
        }
    }

    private void processLines(ReadByLine csvFile, ColumnDefinition columnDefinition)
            throws CSVReaderException {

        csvFile.read(
                (line, firstLine) -> {
                    CSVRow row = columnDefinition.createRow(line);
                    map.put(row.getId(), row.getGroup(), row);
                });
    }

    public Set<String> groupNameSet() {
        return map.getSetGroups();
    }

    public MapGroupToRow get(String key) {
        return map.get(key);
    }
}
