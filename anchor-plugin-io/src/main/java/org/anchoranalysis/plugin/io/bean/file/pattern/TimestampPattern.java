package org.anchoranalysis.plugin.io.bean.file.pattern;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.bean.annotation.BeanField;

/**
 * Extracts a timestamp from a file-name if it exists.
 *
 * <p>A regular-expression specifies whether the file-name matches, and respective groups for year,
 * month, day, hour, minutes, seconds.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor
public class TimestampPattern extends AnchorBean<TimestampPattern> {

    // START BEAN PROPERTIES
    /**
     * The regular-expression that checks a match and specifies groups for the encoded date-time.
     */
    @BeanField @Getter @Setter private String regularExpression;

    /**
     * The index of the group in {@code regularExpression} that encodes the <b>year</b>, starting
     * from 0 for the first group.
     */
    @BeanField @Getter @Setter private int indexYear = 0;

    /**
     * The index of the group in {@code regularExpression} that encodes the <b>month</b>, starting
     * from 0 for the first group.
     */
    @BeanField @Getter @Setter private int indexMonth = 1;

    /**
     * The index of the group in {@code regularExpression} that encodes the <b>day</b>, starting
     * from 0 for the first group.
     */
    @BeanField @Getter @Setter private int indexDay = 2;

    /**
     * The index of the group in {@code regularExpression} that encodes the <b>hours</b>, starting
     * from 0 for the first group.
     */
    @BeanField @Getter @Setter private int indexHours = 3;

    /**
     * The index of the group in {@code regularExpression} that encodes the <b>minutes</b>, starting
     * from 0 for the first group.
     */
    @BeanField @Getter @Setter private int indexMinutes = 4;

    /**
     * The index of the group in {@code regularExpression} that encodes the <b>seconds</b>, starting
     * from 0 for the first group.
     */
    @BeanField @Getter @Setter private int indexSeconds = 5;
    // END BEAN PROPERTIES

    /** The compiled version of {@code regularExpression}. */
    private Pattern pattern;

    public TimestampPattern(String regularExpression) {
        this.regularExpression = regularExpression;
    }

    /**
     * Matches certain times.
     *
     * @param fileName the fileName to try and match against the pattern.
     * @param offset the offset to assume the time-stamp belongs in.
     * @return if the string matches, the number of seconds since the epoch.
     */
    public Optional<Long> match(String fileName, ZoneOffset offset) {
        Matcher matcher = getPatternMemo().matcher(fileName);
        if (matcher.matches()) {
            int year = extractInt(matcher, indexYear);
            int month = extractInt(matcher, indexMonth);
            int day = extractInt(matcher, indexDay);
            int hours = extractInt(matcher, indexHours);
            int minutes = extractInt(matcher, indexMinutes);
            int seconds = extractInt(matcher, indexSeconds);
            LocalDateTime dateTime = LocalDateTime.of(year, month, day, hours, minutes, seconds);
            return Optional.of(dateTime.toEpochSecond(offset));
        } else {
            return Optional.empty();
        }
    }

    /** Gets {@code pattern} compiling from {@regularExpression} if necessary, and memoizing. */
    private Pattern getPatternMemo() {
        if (pattern == null) {
            pattern = Pattern.compile(regularExpression);
        }
        return pattern;
    }

    /** Extracts a particular group from the regular-expression and converts to an {@code int}. */
    private static int extractInt(Matcher matcher, int index) {
        return Integer.parseInt(matcher.group(index + 1));
    }
}
