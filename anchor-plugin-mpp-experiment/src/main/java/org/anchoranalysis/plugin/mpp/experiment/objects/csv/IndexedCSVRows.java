/* (C)2020 */
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
