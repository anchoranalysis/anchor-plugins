/*-
 * #%L
 * anchor-plugin-io
 * %%
 * Copyright (C) 2010 - 2021 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
package org.anchoranalysis.plugin.io.naming.interval;

import java.time.format.DateTimeFormatter;
import org.anchoranalysis.core.exception.friendly.AnchorImpossibleSituationException;

/**
 * How to style a time in a file-name.
 *
 * @author Owen Feehan
 */
public enum TimeStyle {

    /** The entire time is included in the name: {@code hh.mm.ss} */
    INCLUDE_ENTIRELY,

    /** The time is included in the name, but without seconds: {@code hh.mm} */
    IGNORE_SECONDS,

    /** The time is omitted from the name. */
    OMIT;

    private static final String TIME_WITH_SECONDS_PATTERN = "HH.mm.ss";

    private static final String TIME_WITHOUT_SECONDS_PATTERN = "HH.mm";

    /**
     * Derives a formatter for the particular date AND time-style.
     *
     * @param dateStyle whether to include the date or not
     */
    public DateTimeFormatter patternWithDate(DateStyle dateStyle) {

        if (this == OMIT) {
            throw new AnchorImpossibleSituationException();
        }

        String timePattern =
                this == IGNORE_SECONDS ? TIME_WITHOUT_SECONDS_PATTERN : TIME_WITH_SECONDS_PATTERN;

        if (dateStyle != DateStyle.OMIT) {
            return DateTimeFormatter.ofPattern(dateStyle.pattern() + " " + timePattern);
        } else {
            return DateTimeFormatter.ofPattern(timePattern);
        }
    }
}
