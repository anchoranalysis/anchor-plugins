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
package org.anchoranalysis.plugin.io.file.copy;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.plugin.io.bean.file.copy.naming.cluster.ClusterIdentifier;

/**
 * A mapping of files to membership of particular clusters.
 *
 * @author Owen Feehan
 */
@RequiredArgsConstructor
public class ClusterMembership {

    // START REQUIRED ARGUMENTS
    /**
     * The cluster-identifier that is returned for get operations where no file-mapping exists in
     * the map.
     *
     * <p>This can be convenient to describe outliers that have not been placed in any cluster.
     */
    private final ClusterIdentifier clusterIdentifierIfAbsent;
    // END REQUIRED ARGUMENTS

    /** Maps each file to an identifier for a cluster. */
    private Map<File, ClusterIdentifier> map = new HashMap<>();

    /**
     * Indicates a particular file is a member of a particular cluster.
     *
     * @param file the file.
     * @param timestamp the timetamp (seconds since the epoch).
     * @param clusterIdentifier an identifier for the cluster.
     * @throws OperationFailedException if the file is already associated with a cluster.
     */
    public void associateFileWithCluster(
            File file, long timestamp, ClusterIdentifier clusterIdentifier)
            throws OperationFailedException {
        clusterIdentifier.addTimestamp(timestamp);
        if (map.putIfAbsent(file, clusterIdentifier) != null) {
            throw new OperationFailedException(
                    String.format("The file is already associated with a cluster: %s", file));
        }
    }

    /**
     * Finds the corresponding cluster for a particular file.
     *
     * @param file the file to find a cluster for.
     * @return the identifier for the cluster from the map, or {@code clusterIdentifierIfAbsent} (as
     *     per constructor) if the file is not mapped.
     */
    public ClusterIdentifier clusterFor(File file) {
        ClusterIdentifier mapping = map.get(file);
        if (mapping != null) {
            return mapping;
        } else {
            return clusterIdentifierIfAbsent;
        }
    }
}
