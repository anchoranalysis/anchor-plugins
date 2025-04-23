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

package org.anchoranalysis.plugin.io.bean.input.filter;

import java.nio.file.Path;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.io.input.InputsWithDirectory;
import org.anchoranalysis.io.input.bean.InputManagerParameters;
import org.anchoranalysis.io.input.bean.InputManagerUnary;
import org.anchoranalysis.io.input.bean.path.DerivePath;
import org.anchoranalysis.io.input.csv.CSVReaderException;
import org.anchoranalysis.io.input.path.DerivePathException;

/**
 * Finds a CSV file with the names of an input as the first-column.
 *
 * <p>Filters against the value of the second-column of the CSV so that only inputs that match this
 * value are accepted.
 *
 * <p>Each entry from the delegated input-manager must map 1 to 1 to rows in the CSV file
 *
 * @author Owen Feehan
 * @param <T> InputType
 */
public class FilterCSVColumn<T extends InputFromManager> extends InputManagerUnary<T> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private DerivePath csvFilePath;

    @BeanField @Getter @Setter private String match;

    // END BEAN PROPERTIES

    @Override
    protected InputsWithDirectory<T> inputsFromDelegate(
            InputsWithDirectory<T> fromDelegate, InputManagerParameters parameters)
            throws InputReadFailedException {

        if (fromDelegate.isEmpty()) {
            return fromDelegate;
        }

        try {
            List<T> inputs = fromDelegate.inputs();

            Set<String> matching =
                    matchingNames(
                            inputs.get(0).pathForBindingRequired(),
                            parameters.isDebugModeActivated(),
                            inputs.size());

            applyFilter(inputs, matching);
            return fromDelegate;
        } catch (DerivePathException e) {
            throw new InputReadFailedException(e);
        }
    }

    private Set<String> matchingNames(Path pathForGenerator, boolean doDebug, int numRowsExpected)
            throws DerivePathException {
        // Read CSV file using the path of the first object
        Path csvPath = csvFilePath.deriveFrom(pathForGenerator, doDebug);

        try {
            return CsvMatcher.rowsFromCsvThatMatch(csvPath, match, numRowsExpected);
        } catch (CSVReaderException e) {
            throw new DerivePathException("Cannot match rows from csv", e);
        }
    }

    /**
     * Removes all items from the inputList whose input-name is NOT found in {@code mustContain}.
     */
    private void applyFilter(List<T> in, Set<String> mustContain) {

        ListIterator<T> itr = in.listIterator();
        while (itr.hasNext()) {
            T item = itr.next();

            if (!mustContain.contains(item.identifier())) {
                itr.remove();
            }
        }
    }
}
