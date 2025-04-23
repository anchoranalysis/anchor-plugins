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

package org.anchoranalysis.plugin.io.bean.summarizer.input;

import java.nio.file.Path;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.value.StringUtilities;
import org.anchoranalysis.io.input.InputFromManager;

/**
 * Extracts the identifier and path from an {@link InputFromManager} to summarize further.
 *
 * <p>They are combined in the form {@code IDENTIFIER -> PATH}.
 *
 * @author Owen Feehan
 * @param <T> input-type.
 */
public class ExtractIdentifierAndPath<T extends InputFromManager>
        extends SummarizerInputFromManager<T, String> {

    // START BEAN PROPERTIES
    /**
     * The maximum width permitted for an identifier, so that it is printed as one line. Otherwise
     * it is printed as two lines.
     */
    @BeanField @Getter @Setter private int maxPathWidth = 40;

    // END BEAN PROPERTIES

    @Override
    protected Optional<String> extractFrom(T input) {
        String identifier = input.identifier();
        String path = input.pathForBinding().map(Path::toString).orElse("<no identifier>");
        return Optional.of(
                String.format(formatString(path.length()), maybePadPath(path), identifier));
    }

    @Override
    public boolean requiresImageMetadata() {
        return false;
    }

    /** How to display the two strings, one-line or two-lines. */
    private String formatString(int identifierWidth) {
        if (identifierWidth <= maxPathWidth) {
            // One line
            return "%s -> %s";
        } else {
            // Multiline
            return "%s%n   -> %s";
        }
    }

    /**
     * Adds whitespace to the right to bring {@code identifier} to a fixed width, but only if it
     * will be displayed one-line.
     */
    private String maybePadPath(String identifier) {
        if (identifier.length() <= maxPathWidth) {
            return StringUtilities.rightPad(identifier, maxPathWidth);
        } else {
            return identifier;
        }
    }
}
