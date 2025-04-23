/*-
 * #%L
 * anchor-image-experiment
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

package org.anchoranalysis.plugin.image.task.labeller;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.io.input.csv.CSVReaderByLine;
import org.anchoranalysis.io.input.csv.CSVReaderException;
import org.anchoranalysis.io.input.csv.ReadByLine;

/**
 * A map that associates file identifiers with labels, typically loaded from a CSV file.
 *
 * @param <T> the type of the file identifier
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileLabelMap<T> {

    /** The internal map storing file identifiers and their associated labels. */
    private Map<T, String> map = new HashMap<>();

    /**
     * Reads a {@link FileLabelMap} from a CSV file.
     *
     * @param csvPath the {@link Path} to the CSV file
     * @param quotedStrings whether strings in the CSV are quoted
     * @return a new {@link FileLabelMap} instance populated with data from the CSV
     * @throws CSVReaderException if there's an error reading the CSV file
     */
    public static FileLabelMap<String> readFromCSV(Path csvPath, boolean quotedStrings)
            throws CSVReaderException {
        FileLabelMap<String> map = new FileLabelMap<>();

        try (ReadByLine reader = CSVReaderByLine.open(csvPath, ",", true, quotedStrings)) {
            reader.read((line, firstLine) -> map.add(line[0], line[1]));
        }

        return map;
    }

    /**
     * Adds a new file identifier and label pair to the map.
     *
     * @param fileId the file identifier
     * @param label the label associated with the file identifier
     */
    public void add(T fileId, String label) {
        map.put(fileId, label);
    }

    /**
     * Gets the label associated with a file identifier.
     *
     * @param fileId the file identifier
     * @return the label associated with the file identifier, or null if not found
     */
    public String get(T fileId) {
        return map.get(fileId);
    }

    /**
     * Gets a set of all unique labels in the map.
     *
     * @return a {@link Set} of all labels
     */
    public Set<String> labels() {
        return new HashSet<>(map.values());
    }

    /**
     * Gets the entry set of the internal map.
     *
     * @return a {@link Set} of map entries containing file identifiers and labels
     */
    public Set<Entry<T, String>> entrySet() {
        return map.entrySet();
    }
}
