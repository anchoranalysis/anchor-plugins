/*-
 * #%L
 * anchor-plugin-image
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

package org.anchoranalysis.plugin.image.segment.watershed.encoding;

import com.google.common.base.Preconditions;
import java.util.LinkedList;
import java.util.List;

/**
 * Implements a priority queue based upon a range of values from 0 to maxValue.
 *
 * <p>The maximum index value is always taken first and otherwise a FIFO rule applies.
 *
 * @param <T> item-type in priority queue
 */
public class PriorityQueueIndexRangeDownhill<T> {

    private List<T>[] lists;

    private int nextIndexValue = -1;
    private List<T> nextList;

    /**
     * Creates a new priority queue with a specified maximum value.
     *
     * @param maxPossibleValue the maximum possible index value (inclusive)
     * @throws IllegalArgumentException if maxPossibleValue is negative
     */
    @SuppressWarnings("unchecked")
    public PriorityQueueIndexRangeDownhill(int maxPossibleValue) {
        Preconditions.checkArgument(maxPossibleValue >= 0);

        lists = new List[maxPossibleValue + 1];
        for (int i = 0; i <= maxPossibleValue; i++) {
            lists[i] = new LinkedList<>();
        }
    }

    /**
     * Adds an item to the priority queue with a specified index value.
     *
     * @param item the item to add
     * @param indexValue the priority index value for the item
     */
    public void put(T item, int indexValue) {
        List<T> list = lists[indexValue];

        lists[indexValue].add(item);
        if (indexValue > nextIndexValue) {
            nextIndexValue = indexValue;
            nextList = list;
        }
    }

    /**
     * Checks if there are any items left in the queue.
     *
     * @return true if there are items in the queue, false otherwise
     */
    public boolean hasNext() {
        return nextList != null;
    }

    /**
     * Returns the index value of the next item to be retrieved.
     *
     * @return the index value of the next item, or -1 if there is no next value
     */
    public int nextValue() {
        return nextIndexValue;
    }

    /**
     * Retrieves and removes the next item from the queue.
     *
     * @return the next item in the queue, or null if the queue is empty
     */
    public T get() {

        T ret = nextList.remove(0);

        if (nextList.isEmpty()) {
            calculateNewMaxCurrentValue();
        }

        return ret;
    }

    /**
     * Recalculates the maximum current value in the queue. This method is called internally when
     * the current maximum value list becomes empty.
     */
    private void calculateNewMaxCurrentValue() {

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
