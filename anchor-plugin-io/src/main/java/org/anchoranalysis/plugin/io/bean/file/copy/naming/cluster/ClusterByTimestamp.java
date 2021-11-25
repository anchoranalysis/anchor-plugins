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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.exception.friendly.AnchorImpossibleSituationException;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.core.system.path.PathDifferenceException;
import org.anchoranalysis.io.input.file.FileWithDirectoryInput;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.path.prefixer.DirectoryWithPrefix;
import org.anchoranalysis.plugin.io.bean.file.copy.naming.CopyFilesNaming;
import org.anchoranalysis.plugin.io.bean.file.pattern.TimestampPattern;
import org.anchoranalysis.plugin.io.file.copy.ClusterMembership;
import org.anchoranalysis.plugin.io.file.copy.PathOperations;
import org.anchoranalysis.plugin.io.input.path.CopyContext;

/**
 * Associates particular timestamp with each file, and clusters.
 *
 * <p>The timestamp is chosen, in this order of priority:
 *
 * <ul>
 *   <li>A date / time string extracted from the filename, if exists in particular patterns, falling
 *       back to creation-time, if none exists.
 *   <li>Original photo-taken time from EXIF metadata if available, and the file has a <i>jpg</i> or
 *       <i>jpeg extension.</i>
 *   <li><i>File creation time.</i>
 * </ul>
 *
 * <p>Timezones are assumed to be the current time-zone, if not otherwise indicated.
 *
 * <p><i>File modification time</i> is <u>not</u> considered.
 *
 * <p>The clustered are named 01, 02, 03 etc. depending on the number of clusters.
 *
 * <p>The <a href="https://en.wikipedia.org/wiki/DBSCAN">DBSCAN algorithm</a> is used for
 * clustering.
 *
 * <p>A special cluster {@link #OUTLIER_CLUSTER_IDENTIFIER} may also be created, for points that
 * were not density-reachable by others, and aren't part of any cluster in particular.
 *
 * <p>The relative-path of files are preserved, being added relative to the cluster subdirectory.
 *
 * <p>The default-patterns for matching filenames are:
 *
 * <ul>
 *   <li>{@code yyyy-mm-dd hh:mm:ss}
 *   <li>{@code yyyymmdd_hhmmss}
 *   <li>{@code yyyymmdd hhmmss}
 * </ul>
 *
 * @author Owen Feehan
 */
public class ClusterByTimestamp extends CopyFilesNaming<ClusterMembership> {

    /** The name of the cluster for any files considered as outliers by the clustering algorithm. */
    private static final String OUTLIER_CLUSTER_IDENTIFIER = "outliers";

    // START BEAN ARGUMENTS
    /**
     * Files whose creation-time differs {@code <=} this parameter are joined into the same cluster.
     *
     * <p>This is the principle parameter for affecting the sensitivity of the clustering. It is
     * specified in <i>hours</i> between the date-time of two files.
     *
     * <p>A larger value encourages a smaller total number of clusters (or larger cluster-size). A
     * smaller values encourages the opposite.
     */
    @BeanField @Getter @Setter private double thresholdHours = 1.0;

    /** The minimum number of files that must exist for a cluster. */
    @BeanField @Getter @Setter private int minimumPerCluster = 1;

    /**
     * If true, the entire relative-path is used when copying files into the cluster directory. If
     * false, only the file-name is used.
     */
    @BeanField @Getter @Setter private boolean preserveSubdirectories = false;

    /** The patterns which can be used to extract a date-time from a filename. */
    @BeanField @Getter @Setter
    private List<TimestampPattern> timestampPatterns = defaultDateTimePatterns();

    /**
     * If {@code >= 0}, sets a specific time-offset in hours. If {@code == -1}, then the offset is
     * taken from the current system time-zone settings.
     */
    @BeanField @Getter @Setter private int timeZoneOffset = -1;
    // END BEAN ARGUMENTS

    @Override
    public ClusterMembership beforeCopying(
            Path destinationDirectory, List<FileWithDirectoryInput> inputs)
            throws OperationFailedException {

        ZoneOffset offset = offset();

        ClusterMembership membership =
                new ClusterMembership(new ClusterIdentifier(OUTLIER_CLUSTER_IDENTIFIER, offset));

        DeriveTimestampedFiles derive = new DeriveTimestampedFiles(timestampPatterns);

        List<TimestampedFile> timestampedFiles = derive.derive(inputs, offset);

        PopulateClusterMembership populate = new PopulateClusterMembership(membership, offset);
        populate.populateFrom(
                timestampedFiles, derive.getScaler(), offset, thresholdHours, minimumPerCluster);
        return membership;
    }

    @Override
    public Optional<Path> destinationPathRelative(
            File file,
            DirectoryWithPrefix outputTarget,
            int index,
            CopyContext<ClusterMembership> context)
            throws OutputWriteFailedException {
        try {
            return pathForFile(
                    context.getSourceDirectory(),
                    file.toPath(),
                    context.getSharedState().clusterFor(file));

        } catch (PathDifferenceException e) {
            throw new OutputWriteFailedException(e);
        }
    }

    /** The relative-path to copy a file to, relative to the output directory. */
    private Optional<Path> pathForFile(
            Path sourceDirectory, Path file, ClusterIdentifier clusterIdentifier)
            throws PathDifferenceException {
        Path relative = PathOperations.filePathDifference(sourceDirectory, file);
        if (!preserveSubdirectories) {
            relative = relative.getFileName();
        }
        try {
            return Optional.of(Paths.get(clusterIdentifier.name()).resolve(relative));
        } catch (OperationFailedException e) {
            throw new AnchorImpossibleSituationException();
        }
    }

    /** The timezone to use. */
    private ZoneOffset offset() {
        if (timeZoneOffset == -1) {
            return OffsetDateTime.now().getOffset();
        }
        if (timeZoneOffset >= 0) {
            return ZoneOffset.ofHours(timeZoneOffset);
        } else {
            throw new AnchorImpossibleSituationException();
        }
    }

    /**
     * The default list of date-time patterns to use, if none additionally have been set.
     *
     * <p>See the class-level Javadoc for a human-readable description of these patterns.
     */
    private static List<TimestampPattern> defaultDateTimePatterns() {
        List<String> patterns =
                Arrays.asList(
                        ".*(\\d\\d\\d\\d)-(\\d\\d)-(\\d\\d) (\\d\\d)\\.(\\d\\d)\\.(\\d\\d).*",
                        ".*(\\d\\d\\d\\d)(\\d\\d)(\\d\\d)_(\\d\\d)(\\d\\d)(\\d\\d)\\D.*",
                        ".*(\\d\\d\\d\\d)(\\d\\d)(\\d\\d) (\\d\\d)(\\d\\d)(\\d\\d)\\D.*");

        return FunctionalList.mapToList(patterns, TimestampPattern::new);
    }
}
