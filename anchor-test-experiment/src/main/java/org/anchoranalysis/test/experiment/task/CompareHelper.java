/*-
 * #%L
 * anchor-plugin-mpp-experiment
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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

package org.anchoranalysis.test.experiment.task;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.exception.OperationFailedRuntimeException;
import org.anchoranalysis.core.format.ImageFileFormat;
import org.anchoranalysis.core.format.NonImageFileFormat;
import org.anchoranalysis.io.input.csv.CSVReaderException;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.image.DualComparer;
import org.anchoranalysis.test.image.DualComparerFactory;
import org.anchoranalysis.test.image.csv.CSVComparer;

/**
 * Asserts files in the output directory and resources directory are identical.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class CompareHelper {

    private static final CSVComparer CSV_COMPARER = new CSVComparer(",", true, 0, true, false);

    /**
     * Asserts that particular files in an <i>output directory</i> are identical to those in a
     * <i>resources directory</i>.
     *
     * @param absoluteOutput an absolute path to the output directory.
     * @param pathRelativeToResources a path, relative to the maven working directory, where the
     *     resource directory is located.
     * @param relativePaths paths, relative to both directories above, of the specific files to
     *     compare.
     * @throws OperationFailedException if a particular file extension cannot be compared.
     */
    public static void assertDirectoriesIdentical(
            Path absoluteOutput, String pathRelativeToResources, Iterable<String> relativePaths)
            throws OperationFailedException {
        assertDirectoriesIdentical(absoluteOutput, pathRelativeToResources, relativePaths, false);
    }

    /**
     * Like {@link #assertDirectoriesIdentical(Path, String, Iterable)} but additionally exposes the
     * {@code copyNonIdentical} flag.
     *
     * <p>When this flag is true, and an {@link #assertDirectoriesIdentical} fails on a particular
     * file, the first path in the comparer (the newly created file) will be copied on top of the
     * second path in the comparer (the reference file). No assertion will be thrown. If false, no
     * copying occurs.
     *
     * <p>This is a powerful tool to update resources when they are out of sync with the tests.
     * However, it should be used very <b>carefully</b> as it overrides existing files in the
     * source-code directory.
     *
     * @param absoluteOutput an absolute path to the output directory.
     * @param pathRelativeToResources a path, relative to the maven working directory, where the
     *     resource directory is located.
     * @param relativePaths paths, relative to both directories above, of the specific files to
     *     compare.
     * @param copyNonIdentical when true, and two files are not identical, the version in the output
     *     directory is copied into the resources directory.
     * @throws OperationFailedException if a particular file extension cannot be compared, or a
     *     copyNonIdentical operation fails.
     */
    public static void assertDirectoriesIdentical(
            Path absoluteOutput,
            String pathRelativeToResources,
            Iterable<String> relativePaths,
            boolean copyNonIdentical)
            throws OperationFailedException {

        if (copyNonIdentical) {
            // Ensure the directories exist for test comparison
            Path path = TestLoader.pathMavenWorkingDirectory(pathRelativeToResources);
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new OperationFailedException(e);
            }
        }

        DualComparer comparer =
                DualComparerFactory.compareExplicitDirectoryToTest(
                        absoluteOutput, pathRelativeToResources);

        for (String path : relativePaths) {
            assertIdenticalFiles(comparer, path, copyNonIdentical);
        }
    }

    /** Asserts two files, identified by a specific relative path, are identical. */
    private static void assertIdenticalFiles(
            DualComparer comparer, String relativePath, boolean copyNonIdentical)
            throws OperationFailedException {

        if (copyNonIdentical && !comparer.getLoader2().doesPathExist(relativePath)) {
            copyFromTemporaryToResources(comparer, relativePath);
            return; // Exit early
        }

        boolean identical = areFilesIdentical(comparer, relativePath);

        if (copyNonIdentical && !identical) {
            copyFromTemporaryToResources(comparer, relativePath);
        } else {
            assertTrue(identical, () -> relativePath + " is not identical");
        }
    }

    /** Copies a specific file from the output directory to the resources directory. */
    private static void copyFromTemporaryToResources(DualComparer comparer, String relativePath)
            throws OperationFailedException {
        try {
            comparer.copyFromPath1ToPath2(relativePath);
        } catch (IOException e) {
            throw new OperationFailedException(e);
        }
    }

    /** Performs the comparison to determine if two files are identical. */
    private static boolean areFilesIdentical(DualComparer comparer, String relativePath)
            throws OperationFailedException {
        try {
            if (ImageFileFormat.TIFF.matches(relativePath)) {
                return comparer.compareTwoImages(relativePath);
            } else if (ImageFileFormat.PNG.matches(relativePath)) {
                return comparer.compareTwoImages(relativePath);
            } else if (NonImageFileFormat.CSV.matches(relativePath)) {
                return comparer.compareTwoCsvFiles(
                        relativePath, CSV_COMPARER, System.out); // NOSONAR
            } else if (NonImageFileFormat.XML.matches(relativePath)) {
                return comparer.compareTwoXmlDocuments(relativePath);
            } else if (NonImageFileFormat.HDF5.matches(relativePath)) {
                return comparer.compareTwoObjectCollections(relativePath);
            } else {
                throw new OperationFailedRuntimeException("Extension not supported");
            }
        } catch (IOException | CSVReaderException e) {
            throw new OperationFailedException(
                    String.format("Failed to compare relativePath=%s", relativePath), e);
        }
    }
}
