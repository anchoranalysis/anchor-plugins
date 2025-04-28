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

package org.anchoranalysis.plugin.io.input.filter;

import java.util.Iterator;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import lombok.AllArgsConstructor;
import org.anchoranalysis.io.input.InputFromManager;

/**
 * Filters a list of input-objects by the descriptive name.
 *
 * @author Owen Feehan
 */
@AllArgsConstructor
public class FilterDescriptiveNameEqualsContains {

    /** If non-empty, any item that doesn't match this string is filtered away. */
    private String equals;

    /** If non-empty, any item that doesn't contain this string is filtered away. */
    private String contains;

    /**
     * Removes items from the input list that don't match the filter criteria.
     *
     * @param <T> the type of input, extending {@link InputFromManager}
     * @param in the input list to filter
     * @return the filtered list (same object as {@code in}, modified in-place)
     */
    public <T extends InputFromManager> List<T> removeNonMatching(List<T> in) {
        // If no condition is applied, just pass back the entire iterator
        if (!atLeastOneCondition()) {
            return in;
        }

        removeNonMatchingFrom(in, this::combinedPredicate);

        return in;
    }

    /**
     * Removes items from the input list that don't satisfy the given predicate.
     *
     * @param <T> the type of input, extending {@link InputFromManager}
     * @param in the input list to filter
     * @param predicate the predicate to test each item's identifier against
     */
    private static <T extends InputFromManager> void removeNonMatchingFrom(
            List<T> in, Predicate<String> predicate) {

        Iterator<T> itr = in.listIterator();
        while (itr.hasNext()) {
            T item = itr.next();

            if (!predicate.test(item.identifier())) {
                itr.remove();
            }
        }
    }

    /**
     * Checks if at least one filtering condition is set.
     *
     * @return true if either {@code equals} or {@code contains} is non-empty
     */
    private boolean atLeastOneCondition() {
        return !equals.isEmpty() || !contains.isEmpty();
    }

    /**
     * Combines the 'equals' and 'contains' predicates.
     *
     * @param str the string to test
     * @return true if the string satisfies both predicates (or the respective predicate is not set)
     */
    private boolean combinedPredicate(String str) {
        return nonEmptyAndPredicate(str, equals, Object::equals)
                && nonEmptyAndPredicate(str, contains, (ref, test) -> test.contains(ref));
    }

    /**
     * Applies a predicate if the reference string is non-empty.
     *
     * @param strToTest the string to test
     * @param strReference the reference string
     * @param func the predicate function to apply
     * @return true if the reference is empty or the predicate returns true
     */
    private static boolean nonEmptyAndPredicate(
            String strToTest, String strReference, BiPredicate<String, String> func) {
        if (!strReference.isEmpty()) {
            return func.test(strReference, strToTest);
        } else {
            return true;
        }
    }
}
