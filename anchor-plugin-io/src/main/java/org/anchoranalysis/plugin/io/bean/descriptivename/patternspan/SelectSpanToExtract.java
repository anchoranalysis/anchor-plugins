/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.descriptivename.patternspan;

import com.owenfeehan.pathpatternfinder.Pattern;
import com.owenfeehan.pathpatternfinder.patternelements.PatternElement;

/**
 * Selects which elements to extract from a pattern
 *
 * @author Owen Feehan
 */
class SelectSpanToExtract {

    private Pattern pattern;

    public SelectSpanToExtract(Pattern pattern) {
        super();
        this.pattern = pattern;
    }

    public ExtractVariableSpan createExtracter(String elseName) {
        int indexSpanStart = indexFirstVariableElement();
        int indexFinalVariableElement = indexFinalVariableElement();
        assert (indexSpanStart != -1);
        assert (indexFinalVariableElement != -1);

        return new ExtractVariableSpan(
                pattern, elseName, indexSpanStart, selectFinalSpanIndex(indexFinalVariableElement));
    }

    private int selectFinalSpanIndex(int indexFinalVariableElement) {
        if (doElementsAfterContainPeriod(indexFinalVariableElement)) {
            return indexFinalVariableElement;
        } else {
            // The last element
            return pattern.size() - 1;
        }
    }

    /** Checks if all the constant elements > index contain exactly one period (and one ONLY) */
    private boolean doElementsAfterContainPeriod(int index) {
        String remainingStr = extractConstantElementsAfter(index);
        return remainingStr.indexOf('.') != -1;
    }

    /** Extracts the constant elements greater and equal to index as a string */
    private String extractConstantElementsAfter(int index) {
        StringBuilder sb = new StringBuilder();
        for (int i = index + 1; i < pattern.size(); i++) {
            PatternElement element = pattern.get(i);
            assert (element.hasConstantValue());
            sb.append(PatternUtilities.constantElementAsString(element));
        }
        return sb.toString();
    }

    /**
     * Index of the first variable-element (from left to right) in the pattern or -1 if none exist
     */
    private int indexFirstVariableElement() {
        for (int i = 0; i < pattern.size(); i++) {
            if (!pattern.get(i).hasConstantValue()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Index of the first variable-element (from left to right) in the pattern, or -1 if none exists
     */
    private int indexFinalVariableElement() {
        for (int i = (pattern.size() - 1); i >= 0; i--) {
            if (!pattern.get(i).hasConstantValue()) {
                return i;
            }
        }

        return -1;
    }
}
