/* (C)2020 */
package org.anchoranalysis.plugin.mpp.experiment.bean.feature;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.error.OperationFailedRuntimeException;
import org.anchoranalysis.io.csv.comparer.CSVComparer;
import org.anchoranalysis.io.csv.reader.CSVReaderException;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.image.DualComparer;

class CompareHelper {

    private static final CSVComparer CSV_COMPARER = new CSVComparer(",", true, 0, true, false);

    public static void compareOutputWithSaved(
            Path pathAbsoluteOutput, String pathRelativeSaved, String[] relativePaths)
            throws OperationFailedException {
        TestLoader loaderTempDir = TestLoader.createFromExplicitDirectory(pathAbsoluteOutput);
        TestLoader loaderSavedObjects =
                TestLoader.createFromMavenWorkingDirectory(pathRelativeSaved);

        DualComparer comparer = new DualComparer(loaderTempDir, loaderSavedObjects);

        for (String path : relativePaths) {
            assertIdentical(comparer, path);
        }
    }

    private static void assertIdentical(DualComparer comparer, String relativePath)
            throws OperationFailedException {
        assertTrue(relativePath + " is not identical", compareForExtr(comparer, relativePath));
    }

    private static boolean compareForExtr(DualComparer comparer, String relativePath)
            throws OperationFailedException {
        try {
            if (hasExtension(relativePath, ".tif")) {
                return comparer.compareTwoImages(relativePath);
            } else if (hasExtension(relativePath, ".csv")) {
                return comparer.compareTwoCsvFiles(relativePath, CSV_COMPARER, System.out);
            } else if (hasExtension(relativePath, ".xml")) {
                return comparer.compareTwoXmlDocuments(relativePath);
            } else if (hasExtension(relativePath, ".h5")) {
                return comparer.compareTwoObjectCollections(relativePath);
            } else {
                throw new OperationFailedRuntimeException("Extension not supported");
            }
        } catch (IOException | CSVReaderException e) {
            throw new OperationFailedException(e);
        }
    }

    /** Does a string end in an extension, ignoring case? */
    private static boolean hasExtension(String str, String endsWith) {
        return str.toLowerCase().endsWith(endsWith);
    }
}
