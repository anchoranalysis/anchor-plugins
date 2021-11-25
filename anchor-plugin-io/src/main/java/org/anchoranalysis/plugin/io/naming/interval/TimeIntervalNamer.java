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

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import lombok.AllArgsConstructor;

/**
 * Derives a human-friendly name for a time-interval.
 *
 * @author Owen Feehan
 */
@AllArgsConstructor
public class TimeIntervalNamer {

    /** The offset to assume the time-stamp belongs in. */
    private ZoneOffset offset;

    /**
     * Derives a name from the time-range in the cluster and some parameters.
     *
     * <p>If {@code ignoreDate} and {@code ignoreTime} are bothSet, the date is included
     * nevertheless, so the name is not empty.
     *
     * @param start minimum date-time in range
     * @param end maximum date-time in range
     * @param dateStyle how to style the date in the name.
     * @param timeStyle how to style the time in the name.
     * @return the name.
     */
    public String nameFor(
            LocalDateTime start, LocalDateTime end, DateStyle dateStyle, TimeStyle timeStyle) {
        // Spans a single day only
        boolean singleDay = coversSingleDay(start, end);
        if (timeStyle == TimeStyle.OMIT) {
            if (dateStyle == DateStyle.OMIT) {
                // Change the dateStyle so at least one of the date and time is shown
                dateStyle = DateStyle.IGNORE_YEAR;
            }
            if (singleDay) {
                return formatted(start, dateStyle.formatter());
            } else {
                return join(start, end, dateStyle.formatter());
            }
        } else if (dateStyle == DateStyle.OMIT) {
            return joinTime(start, end, dateStyle, timeStyle);

        } else if (singleDay) {
            return formatted(start, dateStyle.formatter())
                    + " "
                    + joinTime(start, end, DateStyle.OMIT, timeStyle);

        } else {
            return joinTime(start, end, dateStyle, timeStyle);
        }
    }

    /**
     * Like {@link #join(LocalDateTime, LocalDateTime, DateTimeFormatter)} but uses a time-only
     * format.
     */
    private String joinTime(
            LocalDateTime start, LocalDateTime end, DateStyle dateStyle, TimeStyle timeStyle) {

        DateTimeFormatter formatter = timeStyle.patternWithDate(dateStyle);

        if (dateStyle == DateStyle.OMIT
                && timeStyle == TimeStyle.IGNORE_SECONDS
                && spansSameMinute(start, end)) {
            // Special case when the range refers to the same minute.
            // We omit the " to " as it's the same minute
            return formatted(start, formatter);
        } else {
            return join(start, end, formatter);
        }
    }

    /** Combines two strings with a " to " between them. */
    private static String join(
            LocalDateTime start, LocalDateTime end, DateTimeFormatter formatter) {
        String first = formatted(start, formatter);
        String second = formatted(end, formatter);
        return String.join(" to ", first, second);
    }

    /** Whether the cluster entirely spans a single day. */
    private static boolean coversSingleDay(LocalDateTime min, LocalDateTime max) {
        return min.toLocalDate().equals(max.toLocalDate());
    }

    /** Formats the date-time in a user-friendly way to insert into a directory-name. */
    private static String formatted(LocalDateTime dateTime, DateTimeFormatter formatter) {
        return formatter.format(dateTime);
    }

    /**
     * Evaluates whether the start and end instances exist in the same minute.
     *
     * @return true iff the start and end timestamps exist in the same minute (since the epoch).
     */
    private boolean spansSameMinute(LocalDateTime start, LocalDateTime end) {
        long startInstant = start.toEpochSecond(offset);
        long endInstant = end.toEpochSecond(offset);
        return (startInstant / 60) == (endInstant / 60);
    }
}
