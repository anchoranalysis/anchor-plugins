package org.anchoranalysis.plugin.io.naming.interval;

import java.time.format.DateTimeFormatter;
import org.anchoranalysis.core.exception.friendly.AnchorImpossibleSituationException;

/**
 * How to style the time in the name.
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
