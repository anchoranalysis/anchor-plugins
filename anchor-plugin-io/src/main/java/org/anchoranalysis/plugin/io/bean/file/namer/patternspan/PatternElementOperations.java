package org.anchoranalysis.plugin.io.bean.file.namer.patternspan;

import com.owenfeehan.pathpatternfinder.Pattern;
import com.owenfeehan.pathpatternfinder.patternelements.PatternElement;
import lombok.AccessLevel;

import lombok.NoArgsConstructor;

/**
 * Operations on the elements of a pattern.
 * 
 * @author Owen Feehan
 *
 */
@NoArgsConstructor(access=AccessLevel.NONE)
class PatternElementOperations {

    /**
     * Count the number of variable-elements in the pattern.
     * 
     * @param pattern the pattern whose elements are iterated over.
     * @return the number of elements in the pattern that are variable (i.e. not constant).
     */
    public static int countVariableElements(Pattern pattern) {
        int count = 0;
        for (int i = 0; i < pattern.size(); i++) {
            if (!pattern.get(i).hasConstantValue()) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Index of the left-most variable-element in the pattern.
     * 
     * @param pattern the pattern whose elements are searched
     * @return the index (zero-indexed) of the left-most variable element or -1 if no variable elements exist.
     */
    public static int indexLeftMostVariableElement(Pattern pattern) {
        return indexLeftMostVariableElement(pattern, 0);
    }
    
    /**
     * Index of the <code>n</code>th left-most variable-element in the pattern.
     * 
     * @param pattern the pattern whose elements are searched
     * @param n (beginning at 0) is an index of all variable elements in the pattern
     * @return the index (zero-indexed) of the <code>n</code>th left-most variable element or -1 if insufficient variable elements exist.
     */
    public static int indexLeftMostVariableElement(Pattern pattern, int n) {
        int count = 0;
        for (int i = 0; i < pattern.size(); i++) {
            if (!pattern.get(i).hasConstantValue()) {
                if (count++==n) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Index of the right-most variable-element in the pattern.
     * 
     * @param pattern the pattern whose elements are searched
     * @return the index (zero-indexed) of the right-most variable element or -1 if no variable elements exist.
     */
    public static int indexRightMostVariableElement(Pattern pattern) {
        for (int i = (pattern.size() - 1); i >= 0; i--) {
            if (!pattern.get(i).hasConstantValue()) {
                return i;
            }
        }

        return -1;
    }
    

    /** 
     * Extracts the constant elements greater and equal to index.
     *
     * @param index all constant elements with indices greater or equal to {@code index} are selected
     * @return the selected constant elements, concatenated together as a string.
     */
    public static String extractConstantElementsAfter(Pattern pattern, int index) {
        StringBuilder sb = new StringBuilder();
        for (int i = index + 1; i < pattern.size(); i++) {
            PatternElement element = pattern.get(i);
            assert (element.hasConstantValue());
            sb.append(PatternUtilities.constantElementAsString(element));
        }
        return sb.toString();
    }
}
