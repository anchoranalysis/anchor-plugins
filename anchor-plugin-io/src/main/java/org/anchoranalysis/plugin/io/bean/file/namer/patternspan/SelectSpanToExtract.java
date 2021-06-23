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

package org.anchoranalysis.plugin.io.bean.file.namer.patternspan;

import java.util.Optional;
import java.util.logging.Logger;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.index.IndexRange;
import com.owenfeehan.pathpatternfinder.Pattern;
import lombok.AllArgsConstructor;

/**
 * Selects which elements to extract from a {@link Pattern}.
 *
 * @author Owen Feehan
 */
@AllArgsConstructor
class SelectSpanToExtract {
    
    private static Logger log = Logger.getLogger(SelectSpanToExtract.class.getName());

    private Pattern pattern;
    
    /** If specifies, determines which variable elements mark the start and end of the extracted span. */
    private Optional<IndexRange> nameSubrange;

    /**
     * Selects which parts of the pattern are included in a {@link ExtractVariableSpan}.
     * 
     * <p>If a {@code nameSubrange} is specified, this determines a range of variables that are included,
     * and any constant elements that occur in between are also included.
     * 
     * <p>Otherwise, the entire range of the extracted variables (from first to last inclusive) is included,
     * including the constant elements inbetween.
     * 
     * @param elseName a fallback to use as name if the extraction fails on an element
     * @return a {@link ExtractVariableSpan} configured to extract across all variable elements.
     */
    public ExtractVariableSpan selectSpanToExtract(String elseName) {
        int numberVariableElements = PatternElementOperations.countVariableElements(pattern);

        return new ExtractVariableSpan(
                pattern, elseName, firstIndex(numberVariableElements), finalIndex(numberVariableElements));
    }
    
    private int firstIndex(int numberVariableElements) {
        if (nameSubrange.isPresent()) {
            try {                
                int variableIndex = nameSubrange.get().correctedStartIndex(numberVariableElements);
                return findVariableElement(variableIndex);
            } catch (OperationFailedException e) {
                // If an error occurred subsetting, then use the left-most element as a fallback.
                log.fine(e.getMessage());
                return PatternElementOperations.indexLeftMostVariableElement(pattern);
            }
        } else {
            return PatternElementOperations.indexLeftMostVariableElement(pattern);
        }
    }
    
    private int finalIndex(int numberVariableElements) {
        if (nameSubrange.isPresent()) {
            try {
                int variableIndex = nameSubrange.get().correctedEndIndex(numberVariableElements);
                return findVariableElement(variableIndex);
            } catch (OperationFailedException e) {
                // If an error occurred subsetting, then use the right-most element as a fallback.
                log.fine(e.getMessage());
                return finalIndexWithoutSubrange();
            }                
        } else {
            return finalIndexWithoutSubrange();
        }
    }
    
    private int finalIndexWithoutSubrange() {
        int rightmost = PatternElementOperations.indexRightMostVariableElement(pattern);
        if (doElementsAfterContainPeriod(rightmost)) {
            return rightmost;
        } else {
            // If there's no file extension in the constant string afterwards, we assume the file extension
            // is part of the variable element, and we thus take the final element irrespective.
            return pattern.size() - 1;
        }        
    }
    
    /** 
     * Translates the index of a variable element to an index of all elements. 
     * 
     * @param variableIndex zero-index, whether its first (0), second (1) third variable element.
     * @return the index across all elements (variable or not) for this variable.
     */
    private int findVariableElement(int variableIndex) {
        return PatternElementOperations.indexLeftMostVariableElement(pattern, variableIndex);
    }

    /** Checks if all the constant elements > index contain exactly one period (and one ONLY) */
    private boolean doElementsAfterContainPeriod(int index) {
        String remainingStr = PatternElementOperations.extractConstantElementsAfter(pattern, index);
        return remainingStr.indexOf('.') != -1;
    }
}
