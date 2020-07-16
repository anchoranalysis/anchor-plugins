/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.input.filter;

import java.nio.file.Path;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.csv.reader.CSVReaderException;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.input.InputFromManager;

/**
 * Finds a CSV file with the descriptiveNames of an input as the first-column
 *
 * <p>Filters against the value of the second-column of the CSV so that only inputs that match this
 * value are accepted.
 *
 * <p>Each entry from the delegated input-manager must map 1 to 1 to rows in the CSV file
 *
 * @author Owen Feehan
 * @param <T> InputType
 */
public class FilterCsvColumn<T extends InputFromManager> extends InputManager<T> {

    // START BEAN PROPERTIES
    @BeanField private InputManager<T> input;

    @BeanField private FilePathGenerator csvFilePath;

    @BeanField private String match;
    // END BEAN PROPERTIES

    @Override
    public List<T> inputObjects(InputManagerParams params) throws AnchorIOException {

        // Existing collection
        List<T> in = input.inputObjects(params);

        if (in.isEmpty()) {
            return in;
        }

        Set<String> matching =
                matchingNames(
                        in.get(0).pathForBindingRequired(),
                        params.isDebugModeActivated(),
                        in.size());

        applyFilter(in, matching);
        return in;
    }

    private Set<String> matchingNames(Path pathForGenerator, boolean doDebug, int numRowsExpected)
            throws AnchorIOException {
        // Read CSV file using the path of the first object
        Path csvPath = csvFilePath.outFilePath(pathForGenerator, doDebug);

        try {
            return CsvMatcher.rowsFromCsvThatMatch(csvPath, match, numRowsExpected);
        } catch (CSVReaderException e) {
            throw new AnchorIOException("Cannot match rows from csv", e);
        }
    }

    // Removes all items from the inputList whose descriptiveName is NOT found in mustContain
    private void applyFilter(List<T> in, Set<String> mustContain) {

        ListIterator<T> itr = in.listIterator();
        while (itr.hasNext()) {
            T item = itr.next();

            if (!mustContain.contains(item.descriptiveName())) {
                itr.remove();
            }
        }
    }

    public InputManager<T> getInput() {
        return input;
    }

    public void setInput(InputManager<T> input) {
        this.input = input;
    }

    public String getMatch() {
        return match;
    }

    public void setMatch(String match) {
        this.match = match;
    }

    public FilePathGenerator getCsvFilePath() {
        return csvFilePath;
    }

    public void setCsvFilePath(FilePathGenerator csvFilePath) {
        this.csvFilePath = csvFilePath;
    }
}
