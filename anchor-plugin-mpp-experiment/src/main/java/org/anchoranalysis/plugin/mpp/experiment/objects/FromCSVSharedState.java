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

package org.anchoranalysis.plugin.mpp.experiment.objects;

import java.nio.file.Path;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.io.input.csv.CSVReaderByLine;
import org.anchoranalysis.io.input.csv.CSVReaderException;
import org.anchoranalysis.io.input.csv.ReadByLine;
import org.anchoranalysis.plugin.mpp.experiment.bean.objects.columndefinition.ColumnDefinition;
import org.anchoranalysis.plugin.mpp.experiment.objects.csv.IndexedCSVRows;

/**
 * Shared-state for {@link
 * org.anchoranalysis.plugin.mpp.experiment.bean.objects.ExportObjectsFromCSV}
 *
 * <p>Mantains an index of the CSV rows, that is initialized first time it's requested.
 *
 * <p>Assumes the CSV input file path will be the same for all the images. If not throws an error.
 */
public class FromCSVSharedState {

    /** An index of the rows in the CSV file, that is cached between different threads */
    private IndexedCSVRows indexedRows = null;

    /**
     * We record the csvFilePath just to check to make sure it's always the same, or otherwise throw
     * an error
     */
    private Path csvFilePathRead;

    public IndexedCSVRows getIndexedRowsOrCreate(
            Path csvFilePath, ColumnDefinition columnDefinition)
            throws GetOperationFailedException {

        if (indexedRows == null) {

            try (ReadByLine reader = CSVReaderByLine.open(csvFilePath)) {
                // Read in our CSV file, and group the rows accordingly
                indexedRows = new IndexedCSVRows(reader, columnDefinition);

                this.csvFilePathRead = csvFilePath;

            } catch (CreateException | CSVReaderException e) {
                throw new GetOperationFailedException(csvFilePath.toString(), e);
            }
        }

        if (!csvFilePath.equals(csvFilePathRead)) {
            throw new GetOperationFailedException(
                    csvFilePath.toString(),
                    String.format(
                            "A previous thread read the CSV file '%s', but this thread looks for '%s'. These files must be identical for all inputs",
                            csvFilePathRead, csvFilePath));
        }

        return indexedRows;
    }
}
