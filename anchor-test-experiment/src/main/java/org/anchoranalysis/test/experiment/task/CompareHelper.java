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

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class CompareHelper {

    /**
     * Toggles a behavior that replaces reference files with newly created ones in certain
     * circumstances.
     *
     * <p>If true, and an {@link #assertIdentical} fails, the first path in the comparer (the newly
     * created file) will be copied on top of the second path in the comparer (the reference file).
     *
     * <p>If false, no copying occurs.
     *
     * <p>This is a powerful tool to update resources when they are out of sync with the tests, but
     * should be used very <b>carefully</b> as it overrides existing files in the source-code
     * directory.
     */
    private static final boolean COPY_NOT_IDENTICAL = false;

    private static final CSVComparer CSV_COMPARER = new CSVComparer(",", true, 0, true, false);

    public static void compareOutputWithSaved(
            Path pathAbsoluteOutput, String pathRelativeSaved, Iterable<String> relativePaths)
            throws OperationFailedException {

        if (COPY_NOT_IDENTICAL) {
            // Ensure the directories exist for test comparison
            Path path = TestLoader.pathMavenWorkingDirectory(pathRelativeSaved);
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new OperationFailedException(e);
            }
        }

        DualComparer comparer =
                DualComparerFactory.compareExplicitDirectoryToTest(
                        pathAbsoluteOutput, pathRelativeSaved);

        for (String path : relativePaths) {
            assertIdentical(comparer, path);
        }
    }

    @SuppressWarnings("unused")
    private static void assertIdentical(DualComparer comparer, String relativePath)
            throws OperationFailedException {

        if (COPY_NOT_IDENTICAL && !comparer.getLoader2().doesPathExist(relativePath)) {
            copyFromTemporaryToResources(comparer, relativePath);
            return; // Exit early
        }

        boolean identical = compareForExtra(comparer, relativePath);

        if (COPY_NOT_IDENTICAL && !identical) {
            copyFromTemporaryToResources(comparer, relativePath);
        } else {
            assertTrue(identical, () -> relativePath + " is not identical");
        }
    }

    private static void copyFromTemporaryToResources(DualComparer comparer, String relativePath)
            throws OperationFailedException {
        try {
            comparer.copyFromPath1ToPath2(relativePath);
        } catch (IOException e) {
            throw new OperationFailedException(e);
        }
    }

    private static boolean compareForExtra(DualComparer comparer, String relativePath)
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
