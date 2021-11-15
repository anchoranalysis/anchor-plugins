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

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.owenfeehan.pathpatternfinder.Pattern;
import com.owenfeehan.pathpatternfinder.patternelements.PatternElement;
import lombok.AllArgsConstructor;
import org.anchoranalysis.core.system.path.FilePathToUnixStyleConverter;

/**
 * Extracts the a <i>spanning part</i> of the pattern from the string.
 *
 * <p>The spanning part should contain all variable elements, and optionally adjacent (or in
 * between) constant elements.
 *
 * <p>It assumes that {@link Pattern} has been derived from a particular list of files, and the
 * <i>spanning part</i> is retrieved for a corresponding index in this list.
 *
 * <p>It uses saved components {@link Pattern} to efficiently perform this operation.
 *
 * @author Owen Feehan
 */
@AllArgsConstructor
class ExtractVariableSpan {

    /** The pattern used to extract the spanning element. */
    private Pattern pattern;

    /** A constant used if failure occurs automatically extracting a descriptive-name. */
    private String elseName;

    /** The index of the first element that should be included in the spanning-part. */
    private int indexSpanStart;

    /** The index of the last element that should be included in the spanning-part. */
    private int indexSpanEnd;

    /**
     * The right-most constant element before the span portion begins.
     *
     * @return a string describing this constant element.
     */
    public String extractConstantElementBeforeSpanPortion() {
        // Find all the constant portions before the empty string
        // Replace any constants from the left-hand-side with empty strings

        if (indexSpanStart == 0) {
            return elseName;
        }

        PatternElement element = pattern.get(indexSpanStart - 1);

        assert (element.hasConstantValue());

        // The index is irrelevant, as it's a constant value, so we use 0.
        return element.valueAt(0);
    }

    /**
     * Extracts the spanning-portion from a particular file, ensuring it has UNIX-style path.
     *
     * @param fileIndex the index of the file (zero-valued) among the strings used for deriving the
     *     pattern.
     * @return the spanning-portion of the file-path corresponding to {@code fileIndex}.
     */
    public String extractSpanPortionFor(int fileIndex) {
        return FilePathToUnixStyleConverter.toStringUnixStyle(
                extractTrimmedFromBothSides(fileIndex));
    }

    /** Extracts the spanning-portion and trims the constant patterns from both left and right. */
    private String extractTrimmedFromBothSides(int fileIndex) {
        String[] elements = pattern.valuesAt(fileIndex, indexSpanStart, indexSpanEnd + 1);
        Preconditions.checkArgument(elements.length == (indexSpanEnd - indexSpanStart + 1));
        return String.join("", elements);
    }
}
