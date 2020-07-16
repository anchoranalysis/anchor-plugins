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
