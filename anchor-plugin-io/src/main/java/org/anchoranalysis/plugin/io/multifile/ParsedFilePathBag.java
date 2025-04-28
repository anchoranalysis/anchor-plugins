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

package org.anchoranalysis.plugin.io.multifile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import org.anchoranalysis.core.functional.OptionalUtilities;

/** A collection of {@link FileDetails} objects with methods to query their properties. */
public class ParsedFilePathBag implements Iterable<FileDetails> {

    /** The list of {@link FileDetails} objects. */
    private List<FileDetails> list = new ArrayList<>();

    /**
     * Adds a {@link FileDetails} object to the collection.
     *
     * @param fileDetails the {@link FileDetails} object to add
     */
    public void add(FileDetails fileDetails) {
        list.add(fileDetails);
    }

    @Override
    public Iterator<FileDetails> iterator() {
        return list.iterator();
    }

    /**
     * Gets the range of channel numbers across all {@link FileDetails}.
     *
     * @return an {@link Optional} containing the {@link IntegerRange} of channel numbers, or empty
     *     if not applicable
     */
    public Optional<IntegerRange> rangeChannelNum() {
        return range(FileDetails::getChannelIndex);
    }

    /**
     * Gets the range of slice numbers across all {@link FileDetails}.
     *
     * @return an {@link Optional} containing the {@link IntegerRange} of slice numbers, or empty if
     *     not applicable
     */
    public Optional<IntegerRange> rangeSliceNum() {
        return range(FileDetails::getSliceIndex);
    }

    /**
     * Gets the range of time indices across all {@link FileDetails}.
     *
     * @return an {@link Optional} containing the {@link IntegerRange} of time indices, or empty if
     *     not applicable
     */
    public Optional<IntegerRange> rangeTimeIndex() {
        return range(FileDetails::getTimeIndex);
    }

    /**
     * Calculates the range of values obtained by applying a function to all {@link FileDetails}.
     *
     * @param func the function to apply to each {@link FileDetails}
     * @return an {@link Optional} containing the {@link IntegerRange} of values, or empty if not
     *     applicable
     */
    @SuppressWarnings("null")
    private Optional<IntegerRange> range(Function<FileDetails, Optional<Integer>> func) {
        return OptionalUtilities.mapBoth(getMin(func), getMax(func), IntegerRange::new);
    }

    /**
     * Gets the maximum value obtained by applying a function to all {@link FileDetails}.
     *
     * @param func the function to apply to each {@link FileDetails}
     * @return an {@link Optional} containing the maximum value, or empty if not applicable
     */
    private Optional<Integer> getMax(Function<FileDetails, Optional<Integer>> func) {
        return fileDetailsStream(func).max(Comparator.naturalOrder());
    }

    /**
     * Gets the minimum value obtained by applying a function to all {@link FileDetails}.
     *
     * @param func the function to apply to each {@link FileDetails}
     * @return an {@link Optional} containing the minimum value, or empty if not applicable
     */
    public Optional<Integer> getMin(Function<FileDetails, Optional<Integer>> func) {
        return fileDetailsStream(func).min(Comparator.naturalOrder());
    }

    /**
     * Creates a stream of integer values obtained by applying a function to all {@link
     * FileDetails}.
     *
     * @param func the function to apply to each {@link FileDetails}
     * @return a {@link Stream} of integer values
     */
    private Stream<Integer> fileDetailsStream(Function<FileDetails, Optional<Integer>> func) {
        return list.stream().map(func).filter(Optional::isPresent).map(Optional::get);
    }

    /**
     * Gets the number of {@link FileDetails} objects in the collection.
     *
     * @return the size of the collection
     */
    public int size() {
        return list.size();
    }
}
