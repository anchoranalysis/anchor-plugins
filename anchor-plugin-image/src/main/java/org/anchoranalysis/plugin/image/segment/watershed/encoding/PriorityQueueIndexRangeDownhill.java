/* (C)2020 */
package org.anchoranalysis.plugin.image.segment.watershed.encoding;

import com.google.common.base.Preconditions;
import java.util.LinkedList;
import java.util.List;

/**
 * Implements a priority queue based upon a range of values from 0 to maxValue,
 *
 * <p>The maximum index value is always taken first and otherwise a FIFO rule applies.
 *
 * @author Owen Feehan
 * @param <T> item-type in priority queue
 */
public class PriorityQueueIndexRangeDownhill<T> {

    private List<T>[] lists;

    private int nextIndexValue = -1;
    private List<T> nextList;

    // The maximum possible grayscale value,
    @SuppressWarnings("unchecked")
    public PriorityQueueIndexRangeDownhill(int maxPossibleValue) {
        Preconditions.checkArgument(maxPossibleValue >= 0);

        lists = new List[maxPossibleValue + 1];
        for (int i = 0; i <= maxPossibleValue; i++) {
            lists[i] = new LinkedList<>();
        }
    }

    public void put(T item, int indexValue) {
        assert (indexValue < lists.length);
        List<T> list = lists[indexValue];

        lists[indexValue].add(item);
        if (indexValue > nextIndexValue) {
            nextIndexValue = indexValue;
            nextList = list;
        }
    }

    public boolean hasNext() {
        return nextList != null;
    }

    // Returns -1 if there is no next value
    public int nextValue() {
        return nextIndexValue;
    }

    // Returns null if there are no items left
    public T get() {

        T ret = nextList.remove(0);

        if (nextList.isEmpty()) {
            calcNewMaxCurrentValue();
        }

        return ret;
    }

    public void calcNewMaxCurrentValue() {

        for (int i = (nextIndexValue - 1); i >= 0; i--) {

            List<T> list = lists[i];
            if (!list.isEmpty()) {
                nextIndexValue = i;
                nextList = list;
                return;
            }
        }

        // We have no more values
        nextIndexValue = -1;
        nextList = null;
    }
}
