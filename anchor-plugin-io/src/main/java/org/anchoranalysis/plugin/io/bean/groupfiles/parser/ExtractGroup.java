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

package org.anchoranalysis.plugin.io.bean.groupfiles.parser;

import java.util.Optional;
import java.util.regex.Matcher;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ExtractGroup {

    public static Optional<String> extractStr(
            int groupID, Matcher matcher, String failureGroupDescription) {

        if (groupID > matcher.groupCount()) {
            return Optional.empty();
        }

        return Optional.of(matcher.group(groupID));
    }

    public static int maybeExtractInt(int groupID, String failureDescr, Matcher matcher) {
        if (groupID > 0) {
            return extractInt(groupID, matcher, failureDescr);
        } else {
            return 0;
        }
    }

    private static int extractInt(int groupID, Matcher matcher, String failureGroupDescription) {

        if (groupID > matcher.groupCount()) {
            return -1;
        }

        Optional<String> groupText = extractStr(groupID, matcher, failureGroupDescription);
        return groupText.map(Integer::parseInt).orElse(-1);
    }
}
