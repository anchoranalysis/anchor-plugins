/* (C)2020 */
package org.anchoranalysis.plugin.io.input.filter;

import java.util.Iterator;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import org.anchoranalysis.io.input.InputFromManager;

/**
 * Filters a list of input-objects by the descriptive name
 *
 * @author Owen Feehan
 */
public class FilterDescriptiveNameEqualsContains {

    private String equals;
    private String contains;

    /**
     * Constructor
     *
     * @param equals if non-empty, any item that doesn't match this string is filtered away
     * @param contains if non-empty, any item that doesn't contain this string is filtered away
     */
    public FilterDescriptiveNameEqualsContains(String equals, String contains) {
        super();
        this.equals = equals;
        this.contains = contains;
    }

    public <T extends InputFromManager> List<T> removeNonMatching(List<T> in) {

        // If no condition is applied, just pass pack the entire iterator
        if (!atLeastOneCondition()) {
            return in;
        }

        removeNonMatchingFrom(in, this::combinedPredicate);

        return in;
    }

    private static <T extends InputFromManager> void removeNonMatchingFrom(
            List<T> in, Predicate<String> predicate) {

        Iterator<T> itr = in.listIterator();
        while (itr.hasNext()) {
            T item = itr.next();

            if (!predicate.test(item.descriptiveName())) {
                itr.remove();
            }
        }
    }

    private boolean atLeastOneCondition() {
        return !equals.isEmpty() || !contains.isEmpty();
    }

    private boolean combinedPredicate(String str) {
        return nonEmptyAndPredicate(str, equals, (ref, test) -> ref.equals(test))
                && nonEmptyAndPredicate(str, contains, (ref, test) -> test.contains(ref));
    }

    private static boolean nonEmptyAndPredicate(
            String strToTest, String strReference, BiPredicate<String, String> func) {
        if (!strReference.isEmpty()) {
            return func.test(strReference, strToTest);
        } else {
            return true;
        }
    }
}
