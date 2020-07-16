/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.input.filter;

import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.io.csv.reader.CSVReaderByLine;
import org.anchoranalysis.io.csv.reader.CSVReaderByLine.ReadByLine;
import org.anchoranalysis.io.csv.reader.CSVReaderException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class CsvMatcher {

    public static Set<String> rowsFromCsvThatMatch(Path path, String match, int numRowsExpected)
            throws CSVReaderException {

        Set<String> set = new TreeSet<>();

        try (ReadByLine csvFile = CSVReaderByLine.open(path, ",", true, true)) {

            int cnt = processLines(csvFile, set, match);

            if (cnt != numRowsExpected) {
                throw new CSVReaderException(
                        String.format(
                                "Csv file must have exactly %d rows. It has %d.",
                                numRowsExpected, cnt));
            }
        }

        return set;
    }

    // Returns the number of lines processed
    private static int processLines(ReadByLine csvFile, Set<String> set, String match)
            throws CSVReaderException {
        return csvFile.read((line, firstLine) -> maybeAddLineToSet(set, line, match));
    }

    private static void maybeAddLineToSet(Set<String> set, String[] line, String match)
            throws OperationFailedException {
        // First column is the name
        // Second column is the value to match against
        if (line.length != 2) {
            throw new OperationFailedException(
                    String.format("Row must have exactly 2 values. It has %d.", line.length));
        }

        if (line[1].equals(match)) {
            addWithDuplicationCheck(set, line[0]);
        }
    }

    private static void addWithDuplicationCheck(Set<String> set, String item)
            throws OperationFailedException {
        if (!set.add(item)) {
            throw new OperationFailedException(
                    String.format("CSV file contains duplicated values for: %s", item));
        }
    }
}
