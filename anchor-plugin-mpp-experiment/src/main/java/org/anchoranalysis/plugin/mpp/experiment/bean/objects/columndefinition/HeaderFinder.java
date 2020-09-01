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

package org.anchoranalysis.plugin.mpp.experiment.bean.objects.columndefinition;

import java.util.Collection;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.functional.CheckedStream;
import org.apache.commons.lang.ArrayUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class HeaderFinder {

    /**
     * Finds headers in a CSV file that describe a particular object-mask
     *
     * @param headers the headers
     * @param columnNameNumberVoxels the column describing the exact number of voxels the object has
     * @param columnNamesPoint three columns (X, Y, Z elements) describing a point that must lie on
     *     the object
     * @return the indices describing these columns if they exist
     * @throws InitException if any one column-name does not exist
     */
    public static ObjectInCsvRowIndices findHeadersToDescribeObject(
            String[] headers, String columnNameNumberVoxels, Collection<String> columnNamesPoint)
            throws InitException {
        return new ObjectInCsvRowIndices(
                findHeaderIndex(headers, columnNameNumberVoxels),
                findHeaderIndices(headers, columnNamesPoint));
    }

    /**
     * Finds the index of a particular column from the headers of a CSV file
     *
     * @param headers the headers
     * @param columnName the column whose index to find
     * @return the index of the first column to be equal (case-sensitive) to {@code columnName}
     * @throws InitException if the column-name does not exist in the headers
     */
    public static int findHeaderIndex(String[] headers, String columnName) throws InitException {
        int index = ArrayUtils.indexOf(headers, columnName);
        if (index == ArrayUtils.INDEX_NOT_FOUND) {
            throw new InitException(
                    String.format("Cannot find column '%s' among CSV file headers", columnName));
        }
        return index;
    }

    /**
     * Like {@link #findHeaderIndex} but can find several headers from a collection.
     *
     * @param headers the headers
     * @param columnNames the column-names to find indices for
     * @return an array of indices, each element corresponding to that in {@code columnNames}
     *     respectively
     * @throws InitException if any column-name does not exist in the headers
     */
    private static int[] findHeaderIndices(String[] headers, Collection<String> columnNames)
            throws InitException {
        return CheckedStream.mapToInt(
                        columnNames.stream(),
                        InitException.class,
                        columnName -> findHeaderIndex(headers, columnName))
                .toArray();
    }
}
