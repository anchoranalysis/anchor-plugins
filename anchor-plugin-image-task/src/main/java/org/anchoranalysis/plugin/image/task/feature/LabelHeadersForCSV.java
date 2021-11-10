/*-
 * #%L
 * anchor-plugin-image-task
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

package org.anchoranalysis.plugin.image.task.feature;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.feature.io.results.LabelHeaders;

/**
 * Generates a list of header-names for columns unrelated to features (identifiers and groups)
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access=AccessLevel.PRIVATE)
public class LabelHeadersForCSV {

    /**
     * The standard group header that occurs if a groups are enabled (with possibly an additional
     * group header.
     */
    private static final String GROUP_HEADER_STANDARD = "group";

    /**
     * Creates the label-headers (the headers for columns not associated with features).
     *
     * @param nonGroupHeaders headers that identify the row unrelated to groups.
     * @param additionalGroupHeader additional group-header used after the {@link #GROUP_HEADER_STANDARD} if groups are enabled.
     * @param groupsEnabled whether groups are enabled or not. Iff true, group headers are included.
     * @return the headers.
     */
    public static LabelHeaders createHeaders(String[] nonGroupHeaders, Optional<String> additionalGroupHeader, boolean groupsEnabled) {
        return new LabelHeaders(nonGroupHeaders, headersForGroup(additionalGroupHeader, groupsEnabled));
    }

    private static String[] headersForGroup(Optional<String> additionalGroupHeader, boolean groupsEnabled) {
        if (groupsEnabled) {
            if (additionalGroupHeader.isPresent()) {
                return new String[] {GROUP_HEADER_STANDARD, additionalGroupHeader.get()};
            } else {
                return new String[] {GROUP_HEADER_STANDARD};
            }
        } else {
            if (additionalGroupHeader.isPresent()) {
                return new String[] {additionalGroupHeader.get()};
            } else {
                return new String[] {};
            }
        }
    }
}
