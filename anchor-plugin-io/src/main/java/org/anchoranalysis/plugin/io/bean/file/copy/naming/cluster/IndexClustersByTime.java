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
import java.util.List;
import org.anchoranalysis.spatial.rtree.IntervalRTree;

/**
 * Indexes a range of clusters across by their <i>minutes since the epoch</i>.
 *
 * <p>Minutes since the epoch is used as the index, with an R-Tree across intervals for efficient
 * queries.
 *
 * @author Owen Feehan
 */
class IndexClustersByTime {

    /** The R-Tree, indexing clusters by <i>minutes since the epoch</i>. */
    private final IntervalRTree<ClusterIdentifier> tree;

    /**
     * Creates for a lister of {@link ClusterIdentifier}s.
     *
     * @param identifiers the identifiers.
     */
    public IndexClustersByTime(List<ClusterIdentifier> identifiers) {
        this.tree = new IntervalRTree<>(identifiers.size());
        for (ClusterIdentifier identifier : identifiers) {
            tree.add(
                    epochMinute(identifier.getMinInstant()),
                    epochMinute(identifier.getMinInstant()),
                    identifier);
        }
    }

    /**
     * Whether a particular {@code identifier} is the only cluster to use the particular days it
     * spans?
     *
     * @return true if {@code identifier} is the only {@link ClusterIdentifier} to use the
     *     <b>days</b> it spans.
     */
    public boolean usesDaysExclusively(ClusterIdentifier identifier) {
        // start the range at the 00:00:00 time on the same day
        LocalDateTime startDay = identifier.getMinTime().toLocalDate().atTime(0, 0, 0);
        LocalDateTime endDay = identifier.getMaxTime().toLocalDate().atTime(23, 59, 59);

        return usesRangeExclusively(
                epochMinute(startDay, identifier.getOffset()),
                epochMinute(endDay, identifier.getOffset()));
    }

    /**
     * Whether a particular {@code identifier} is the only cluster to use the particular minutes it
     * spans?
     *
     * @return true if {@code identifier} is the only {@link ClusterIdentifier} to use the
     *     <b>minutes</b> it spans.
     */
    public boolean usesMinutesExclusively(ClusterIdentifier identifier) {
        return usesRangeExclusively(
                epochMinute(identifier.getMinInstant()), epochMinute(identifier.getMaxInstant()));
    }

    /**
     * Whether a particular range is used only by one cluster?
     *
     * @param start start of range (inclusive) in <i>minutes from the epoch</i>.
     * @param end end of range (inclusive) in <i>minutes from the epoch</i>.
     */
    private boolean usesRangeExclusively(long start, long end) {
        int numberIntersecting = tree.intersectsWith(start, end).size();
        assert (numberIntersecting > 0);
        return numberIntersecting == 1;
    }

    /** Number of minutes since the epoch for {@link LocalDateTime}. */
    private long epochMinute(LocalDateTime timestamp, ZoneOffset offset) {
        return epochMinute(timestamp.toEpochSecond(offset));
    }

    /** Number of minutes since the epoch for <i>seconds since the epoch</i>. */
    private static long epochMinute(long instant) {
        return instant / 60;
    }
}
