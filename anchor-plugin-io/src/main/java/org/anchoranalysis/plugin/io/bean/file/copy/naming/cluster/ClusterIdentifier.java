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
package org.anchoranalysis.plugin.io.bean.file.copy.naming.cluster;

import java.time.LocalDateTime;
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

    /** The offset to assume the time-stamp belongs in. */
    @Getter private final ZoneOffset offset;

    /**
     * Creates with no name.
     *
     * <p>The name is subsequently derived from the contents of the cluster.
     *
     * @param offset the offset to assume the time-stamp belongs in.
     */
    public ClusterIdentifier(ZoneOffset offset) {
        this.name = Optional.empty();
        this.offset = offset;
    }

    /**
     * Creates with a constant-name.
     *
     * @param name the unique name for the cluster.
     * @param offset the offset to assume the time-stamp belongs in.
     */
    public ClusterIdentifier(String name, ZoneOffset offset) {
        this.name = Optional.of(name);
        this.offset = offset;
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
    private LocalDateTime toDate(long instant) {
        return LocalDateTime.ofEpochSecond(instant, 0, offset);
    }
}
