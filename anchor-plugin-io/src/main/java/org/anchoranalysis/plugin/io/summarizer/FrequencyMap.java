/* (C)2020 */
package org.anchoranalysis.plugin.io.summarizer;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.anchoranalysis.core.text.LanguageUtilities;

/**
 * Counts the frequency of certain strings, and describes the contents in human language.
 *
 * @author Owen Feehan
 * @param T key-type (should have .toString() representation that is meaningful to humans)
 */
public class FrequencyMap<T> {

    private Map<T, Integer> map = new TreeMap<>();

    public synchronized void incrCount(T key) {

        Integer cnt = map.get(key);

        if (cnt == null) {
            map.put(key, 1);
        } else {
            map.put(key, cnt + 1);
        }
    }

    public synchronized String describe(String dscrNoun) {
        int numKeys = map.keySet().size();

        if (numKeys == 0) {
            return "No inputs have been found yet.";
        } else if (numKeys == 1) {
            return String.format("with uniform %s = %s", dscrNoun, map.keySet().iterator().next());
        } else {
            return describeDiverse(dscrNoun);
        }
    }

    private String describeDiverse(String dscrNoun) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("with diverse %s:", LanguageUtilities.pluralize(dscrNoun)));

        for (Entry<T, Integer> entry : map.entrySet()) {
            sb.append(String.format(" %s(%d)", entry.getKey(), entry.getValue()));
        }

        return sb.toString();
    }
}
