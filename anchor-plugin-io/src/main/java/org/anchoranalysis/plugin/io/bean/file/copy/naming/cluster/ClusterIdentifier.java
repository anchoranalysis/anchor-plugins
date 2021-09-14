package org.anchoranalysis.plugin.io.bean.file.copy.naming.cluster;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import lombok.Getter;
import org.anchoranalysis.core.exception.OperationFailedException;

/**
 * Uniquely identifies a cluster.
 *
 * <p>We deliberately rely on default {@link #hashCode()} and {@link #equals(Object)} so each
 * instance of the class is unique in a map.
 *
 * <p>It assumes no two clusters will have the same min and max date-time.
 *
 * @author Owen Feehan
 */
public class ClusterIdentifier {

    /**
     * A timezone is needed for certain time conversions. We always assume the zone is that of the
     * current VM.
     */
    private static final ZoneOffset ZONE_OFFSET = OffsetDateTime.now().getOffset();

    /**
     * The unique-name of the cluster-identifier, or {@link Optional#empty} if no name has been
     * assigned yet.
     */
    private Optional<String> name;

    /**
     * Minimum timestamp in cluster. This is the <i>earliest time</i> of any file in the cluster.
     */
    @Getter private long minInstant = Long.MAX_VALUE;

    /** Maximum timestamp in cluster. This is the <i>latest time</i> of any file in the cluster. */
    @Getter private long maxInstant = Long.MIN_VALUE;

    /** A minimum {@link LocalDateTime}, converted from {@code minInstant} when first accessed. */
    private LocalDateTime minDateTime;

    /** A maximum {@link LocalDateTime}, converted from {@code maxInstant} when first accessed. */
    private LocalDateTime maxDateTime;

    /**
     * Creates with no name.
     *
     * <p>The name is subsequently derived from the contents of the cluster.
     */
    public ClusterIdentifier() {
        this.name = Optional.empty();
    }

    /**
     * Creates with a constant-name.
     *
     * @param name the unique name for the cluster.
     */
    public ClusterIdentifier(String name) {
        this.name = Optional.of(name);
    }

    /**
     * Adds a timestamp to the cluster.
     *
     * @param timestamp the timestamp (seconds since the epoch).
     */
    public void addTimestamp(long timestamp) {
        if (timestamp < minInstant) {
            minInstant = timestamp;
        }

        if (timestamp > maxInstant) {
            maxInstant = timestamp;
        }
    }

    /**
     * Assigns a name for the cluster.
     *
     * @param name the name to assign.
     */
    public void assignName(String name) {
        this.name = Optional.of(name);
    }

    /**
     * Derives a name for the cluster, based upon its contents.
     *
     * @return the derived name.
     * @throws OperationFailedException if {@link #assignName(String)} has not occurred.
     */
    public String name() throws OperationFailedException {
        if (name.isPresent()) {
            return name.get();
        } else {
            throw new OperationFailedException("No name has been assigned");
        }
    }

    /**
     * Gets the earliest date-time in the cluster.
     *
     * <p>This is computed the first time the method is called, and remembered for subsequent calls.
     *
     * @return the minimum date-time in the cluster.
     */
    public LocalDateTime getMinTime() {
        if (minDateTime == null) {
            minDateTime = toDate(minInstant);
        }
        return minDateTime;
    }

    /**
     * Gets the latest date-time in the cluster.
     *
     * <p>This is computed the first time the method is called, and remembered for subsequent calls.
     *
     * @return the maximum date-time in the cluster.
     */
    public LocalDateTime getMaxTime() {
        if (maxDateTime == null) {
            maxDateTime = toDate(maxInstant);
        }
        return maxDateTime;
    }

    /** Converts the instant (seconds from the epoch) to a {@link LocalDateTime}. */
    private static LocalDateTime toDate(long instant) {
        return LocalDateTime.ofEpochSecond(instant, 0, ZONE_OFFSET);
    }
}
