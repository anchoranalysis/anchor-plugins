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

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.plugin.io.naming.interval.DateStyle;
import org.anchoranalysis.plugin.io.naming.interval.TimeIntervalNamer;
import org.anchoranalysis.plugin.io.naming.interval.TimeStyle;

/**
 * Establishes names for the clusters.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class EstablishClusterNames {

    /**
     * This calls the {@link ClusterIdentifier#name} function for the first time, establishing a
     * cached name per {@link ClusterIdentifier}.
     *
     * <p>It also checks the name of all clusters are unique.
     *
     * @throws OperationFailedException if the cluster names are not unique.
     */
    public static void assignNames(List<ClusterIdentifier> identifiers)
            throws OperationFailedException {

        Set<String> namesAsSet = createNameSet(identifiers, dateStyle(identifiers));

        // Checks we have uniqueness, in case of a weird circumstance where two clusters have the
        // same minimum and maximum timestamps.
        if (namesAsSet.size() != identifiers.size()) {
            throw new OperationFailedException(
                    "The names of the clusters are not unique, abandoning. This shouldn't happen so is indicative of an internal error.");
        }
    }

    /** Determines how to style the date in the naming for each cluster. */
    private static DateStyle dateStyle(List<ClusterIdentifier> identifiers) {
        // Check if the year is identical, in which case we can ignore it in cluster naming.
        if (hasUniformYear(identifiers)) {

            // Check if the day-of-the-year is identical, in which case we can ignore the date
            // completely in cluster naming.
            if (hasUniformDay(identifiers)) {
                return DateStyle.OMIT;
            } else {
                return DateStyle.IGNORE_YEAR;
            }
        } else {
            return DateStyle.INCLUDE_ENTIRELY;
        }
    }

    /**
     * Creates a set with the names of all clusters.
     *
     * <p>As a side-effect this establishes the name in each {@code identifier}.
     *
     * @param identifiers the identifiers of the clusters.
     * @param dateStyle how to style the date in the name.
     * @return a newly created set, with the name of each cluster.
     */
    private static Set<String> createNameSet(
            List<ClusterIdentifier> identifiers, DateStyle dateStyle) {

        IndexClustersByTime treeMinutes = new IndexClustersByTime(identifiers);

        return identifiers.stream()
                .map(identifier -> createNameAndAssign(identifier, dateStyle, treeMinutes))
                .collect(Collectors.toSet());
    }

    /** Creates a name for the cluster and assigns it to {@code identifier}. */
    private static String createNameAndAssign(
            ClusterIdentifier identifier, DateStyle dateStyle, IndexClustersByTime tree) {

        String name =
                new TimeIntervalNamer(identifier.getOffset())
                        .nameFor(
                                identifier.getMinTime(),
                                identifier.getMaxTime(),
                                dateStyle,
                                timeStyle(identifier, tree));
        identifier.assignName(name);
        return name;
    }

    /** How the time should be styled in the name. */
    private static TimeStyle timeStyle(ClusterIdentifier identifier, IndexClustersByTime tree) {
        if (tree.usesDaysExclusively(identifier)) {
            return TimeStyle.OMIT;
        } else {
            if (tree.usesMinutesExclusively(identifier)) {
                return TimeStyle.IGNORE_SECONDS;
            } else {
                return TimeStyle.INCLUDE_ENTIRELY;
            }
        }
    }

    /** Do all the clusters refer to the same year? */
    private static boolean hasUniformYear(List<ClusterIdentifier> clusters) {
        int running = clusters.get(0).getMinTime().getYear();
        for (ClusterIdentifier identifier : clusters) {
            if (identifier.getMinTime().getYear() != running) {
                return false;
            }
            if (identifier.getMaxTime().getYear() != running) {
                return false;
            }
        }
        return true;
    }

    /** Do all the clusters refer to the same day? */
    private static boolean hasUniformDay(List<ClusterIdentifier> clusters) {
        LocalDate running = clusters.get(0).getMinTime().toLocalDate();
        for (ClusterIdentifier identifier : clusters) {
            if (!identifier.getMinTime().toLocalDate().equals(running)) {
                return false;
            }
            if (!identifier.getMaxTime().toLocalDate().equals(running)) {
                return false;
            }
        }
        return true;
    }
}
