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
