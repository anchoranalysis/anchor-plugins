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

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.AllArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.math.statistics.MeanScale;
import org.anchoranalysis.plugin.io.file.copy.ClusterMembership;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;

/**
 * Populates {@link ClusterMembership} for a list of {@link TimestampedFile}s after clustering.
 *
 * <p>The clustering is performed with the DB-scan algorithm.
 *
 * @author Owen Feehan
 */
@AllArgsConstructor
class PopulateClusterMembership {

    private static final int SECONDS_IN_HOUR = 60 * 60;

    /** The {@link ClusterMembership} that is populated. */
    private ClusterMembership membership;

    /** The offset to assume the time-stamp belongs in. */
    private ZoneOffset offset;

    /**
     * Establishes cluster-membership for the following files.
     *
     * @param files the files.
     * @param scaler the mean-scale associated with {@code files}.
     * @param offset the offset to assume the time-stamp belongs in.
     * @param thresholdHours files whose creation-time differs {@code <=} this parameter are joined
     *     into the same cluster.
     * @param minimumPerCluster the minimum number of neighbors that must exist to form a cluster
     *     (so 1 means 2 files are the minimum for a viable cluster).
     * @throws OperationFailedException
     */
    public void populateFrom(
            List<TimestampedFile> files,
            MeanScale scaler,
            ZoneOffset offset,
            double thresholdHours,
            int minimumPerCluster)
            throws OperationFailedException {
        if (scaler.getScale() >= 0) {
            addAfterClustering(
                    files, calculateEps(thresholdHours, scaler.getScale()), minimumPerCluster);
        } else {
            addToSingleCluster(files, offset);
        }
    }

    /** Add all files to a single-cluster in {@code mapping}. */
    private void addToSingleCluster(List<TimestampedFile> files, ZoneOffset offset)
            throws OperationFailedException {
        ClusterIdentifier identifier = new ClusterIdentifier(offset);
        for (TimestampedFile file : files) {
            membership.associateFileWithCluster(file.getFile(), file.getTimestamp(), identifier);
        }
        EstablishClusterNames.assignNames(Arrays.asList(identifier));
    }

    /**
     * Add files to {@code mapping} after running a clustering algorithm to partition the files.
     *
     * <p>Note that every file in {@code files} is guaranteed to be assigned to a cluster and be put
     * into {@code mapping}.
     *
     * <p>Files that are not added are treated as outliers, and eventually placed in a separate
     * outlier cluster.
     */
    private void addAfterClustering(List<TimestampedFile> files, double eps, int minimumPerCluster)
            throws OperationFailedException {

        DBSCANClusterer<TimestampedFile> clusterer = new DBSCANClusterer<>(eps, minimumPerCluster);
        List<Cluster<TimestampedFile>> clusters = clusterer.cluster(files);

        if (clusters.isEmpty()) {
            // Nothing more to do
            return;
        }

        List<ClusterIdentifier> identifiers = new ArrayList<>();
        for (Cluster<TimestampedFile> cluster : clusters) {
            identifiers.add(addClusterToMapping(cluster));
        }

        EstablishClusterNames.assignNames(identifiers);
    }

    /** Adds one {@link Cluster} to {@code mapping}. */
    private ClusterIdentifier addClusterToMapping(Cluster<TimestampedFile> cluster)
            throws OperationFailedException {
        ClusterIdentifier clusterIdentifier = new ClusterIdentifier(offset);
        for (TimestampedFile file : cluster.getPoints()) {
            membership.associateFileWithCluster(
                    file.getFile(), file.getTimestamp(), clusterIdentifier);
        }
        return clusterIdentifier;
    }

    /** Calculates the eps parameter for DB-scan. */
    private static double calculateEps(double thresholdHours, double scale) {
        return (thresholdHours * SECONDS_IN_HOUR) / scale;
    }
}
