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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.io.input.bean.InputManagerParameters;
import org.anchoranalysis.io.input.bean.files.SearchDirectory;
import org.anchoranalysis.io.input.file.FileWithDirectoryInput;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.path.prefixer.DirectoryWithPrefix;
import org.anchoranalysis.plugin.io.bean.file.namer.LastDirectories;
import org.anchoranalysis.plugin.io.bean.input.files.NamedFilesWithDirectory;
import org.anchoranalysis.plugin.io.file.copy.ClusterMembership;
import org.anchoranalysis.plugin.io.input.path.CopyContext;
import org.anchoranalysis.test.LoggerFixture;
import org.anchoranalysis.test.TestLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests {@link ClusterByTimestamp}.
 *
 * @author Owen Feehan
 */
class ClusterByTimestampTest {

    @TempDir Path directory;

    private TestLoader testLoader =
            TestLoader.createFromMavenWorkingDirectory("toCluster/timestamp");

    private static final List<Path> EXPECTED_DESTINATION_PATHS =
            Arrays.asList(
                    Paths.get("Aug-01 18.45", "IMG_3812.JPG"),
                    Paths.get("Aug-01 18.45", "IMG_3813.JPG"),
                    Paths.get("Jul-31", "IMG_3620.JPG"),
                    Paths.get("Aug-01 15.06 to 15.21", "IMG_3765.JPG"),
                    Paths.get("Aug-01 18.45", "IMG_3815.JPG"),
                    Paths.get("Jul-31", "IMG_3622.JPG"),
                    Paths.get("Aug-01 15.06 to 15.21", "IMG_3767.JPG"),
                    Paths.get("Aug-01 15.06 to 15.21", "IMG_3770.JPG"));

    /** Check if the same set of paths are produced as expected. */
    @Test
    void testDeterminePaths()
            throws OperationFailedException, InputReadFailedException, OutputWriteFailedException {
        List<FileWithDirectoryInput> inputs = createInputs(testLoader.getRoot());

        Set<Path> paths = determineDestinationPaths(inputs);
        assertEquals(expectedPaths(), paths);
    }

    /**
     * Apply the {@link ClusterByTimestamp} algorithm on all files in {@code inputs} and collect the
     * produced paths.
     */
    private Set<Path> determineDestinationPaths(List<FileWithDirectoryInput> inputs)
            throws OperationFailedException, OutputWriteFailedException {
        Set<Path> paths = new HashSet<>();

        ClusterByTimestamp cluster = new ClusterByTimestamp();

        // The timezone is explicitly fixed, so the the test always produces the same results,
        //   irrespective of the system settings.
        cluster.setTimeZoneOffset(2);

        ClusterMembership sharedState = cluster.beforeCopying(directory, inputs);
        CopyContext<ClusterMembership> context =
                new CopyContext<ClusterMembership>(testLoader.getRoot(), directory, sharedState);
        for (FileWithDirectoryInput input : inputs) {
            Optional<Path> destinationPath =
                    cluster.destinationPathRelative(
                            input.getFile(), new DirectoryWithPrefix(directory), 0, context);
            destinationPath.ifPresent(paths::add);
        }
        return paths;
    }

    /** Create inputs. */
    private static List<FileWithDirectoryInput> createInputs(Path source)
            throws InputReadFailedException {
        InputManagerParameters parameters =
                new InputManagerParameters(LoggerFixture.suppressedOperationContext());

        return createFiles(source).inputs(parameters).inputs();
    }

    /** Create the files to be used as inputs. */
    private static NamedFilesWithDirectory createFiles(Path source) {
        NamedFilesWithDirectory files = new NamedFilesWithDirectory();
        files.setNamer(new LastDirectories());
        files.setFiles(new SearchDirectory(source.toString()));
        return files;
    }

    /** The paths expected as an output after clustering. */
    private static Set<Path> expectedPaths() {
        return EXPECTED_DESTINATION_PATHS.stream().collect(Collectors.toSet());
    }
}
