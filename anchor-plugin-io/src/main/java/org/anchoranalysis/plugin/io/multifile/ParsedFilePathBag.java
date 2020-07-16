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
/* (C)2020 */
package org.anchoranalysis.plugin.io.multifile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import org.anchoranalysis.core.functional.OptionalUtilities;

public class ParsedFilePathBag implements Iterable<FileDetails> {

    private List<FileDetails> list = new ArrayList<>();

    // chnlNum and sliceNum can be null, indicating that we don't know the values
    public void add(FileDetails fileDetails) {
        list.add(fileDetails);
    }

    public Iterator<FileDetails> iterator() {
        return list.iterator();
    }

    public Optional<IntegerRange> rangeChnlNum() {
        return range(FileDetails::getChnlNum);
    }

    public Optional<IntegerRange> rangeSliceNum() {
        return range(FileDetails::getSliceNum);
    }

    public Optional<IntegerRange> rangeTimeIndex() {
        return range(FileDetails::getTimeIndex);
    }

    private Optional<IntegerRange> range(Function<FileDetails, Optional<Integer>> func) {
        return OptionalUtilities.mapBoth(getMin(func), getMax(func), IntegerRange::new);
    }

    private Optional<Integer> getMax(Function<FileDetails, Optional<Integer>> func) {
        return fileDetailsStream(func).max(Comparator.naturalOrder());
    }

    public Optional<Integer> getMin(Function<FileDetails, Optional<Integer>> func) {
        return fileDetailsStream(func).min(Comparator.naturalOrder());
    }

    private Stream<Integer> fileDetailsStream(Function<FileDetails, Optional<Integer>> func) {
        return list.stream().map(func).filter(Optional::isPresent).map(Optional::get);
    }

    public int size() {
        return list.size();
    }
}
