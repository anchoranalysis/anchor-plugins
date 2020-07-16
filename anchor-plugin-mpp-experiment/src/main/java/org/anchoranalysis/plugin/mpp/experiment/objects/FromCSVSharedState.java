/* (C)2020 */
package org.anchoranalysis.plugin.mpp.experiment.objects;

import java.nio.file.Path;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.io.csv.reader.CSVReaderByLine;
import org.anchoranalysis.io.csv.reader.CSVReaderByLine.ReadByLine;
import org.anchoranalysis.io.csv.reader.CSVReaderException;
import org.anchoranalysis.plugin.mpp.experiment.bean.objects.columndefinition.ColumnDefinition;
import org.anchoranalysis.plugin.mpp.experiment.objects.csv.IndexedCSVRows;

/**
 * Shared-state for {@link
 * org.anchoranalysis.plugin.mpp.experiment.bean.objects.ExportObjectsFromCSVTask}
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
                throw new GetOperationFailedException(e);
            }
        }

        if (!csvFilePath.equals(csvFilePathRead)) {
            throw new GetOperationFailedException(
                    String.format(
                            "A previous thread read the CSV file '%s', but this thread looks for '%s'. These files must be identical for all inputs",
                            csvFilePathRead, csvFilePath));
        }

        return indexedRows;
    }
}
