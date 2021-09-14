package org.anchoranalysis.plugin.io.bean.file.copy.naming.cluster;

import java.time.format.DateTimeFormatter;
import org.anchoranalysis.core.exception.friendly.AnchorImpossibleSituationException;

/**
 * How to style the date in the name.
 *
 * @author Owen Feehan
 */
public enum DateStyle {

    /** The entire date is included in the name: {@code yyyy-mm-dd} */
    INCLUDE_ENTIRELY,

    /** The date is included in the name, but without the year: {@code mmm-dd} */
    IGNORE_YEAR,

    /** The date is omitted from the name. */
    OMIT;

    private static final String DATE_WITH_YEAR_PATTERN = "yyyy-MM-dd";

    private static final String DATE_WITHOUT_YEAR_PATTERN = "MMM-dd";

    /** Derives a formatter for the particular date-style. */
    public DateTimeFormatter formatter() {
        if (this == OMIT) {
            throw new AnchorImpossibleSituationException();
        } else {
            return DateTimeFormatter.ofPattern(pattern());
        }
    }

    /** Derives a pattern for the formatter for the particular date-style. */
    public String pattern() {
        if (this == INCLUDE_ENTIRELY) {
            return DATE_WITH_YEAR_PATTERN;
        } else if (this == IGNORE_YEAR) {
            return DATE_WITHOUT_YEAR_PATTERN;
        } else {
            throw new AnchorImpossibleSituationException();
        }
    }
}
