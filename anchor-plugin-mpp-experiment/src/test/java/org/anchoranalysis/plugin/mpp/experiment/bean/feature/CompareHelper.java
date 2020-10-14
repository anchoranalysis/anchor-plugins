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

package org.anchoranalysis.plugin.mpp.experiment.bean.feature;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.error.OperationFailedRuntimeException;
import org.anchoranalysis.io.input.csv.CSVReaderException;
import org.anchoranalysis.test.image.DualComparer;
import org.anchoranalysis.test.image.DualComparerFactory;
import org.anchoranalysis.test.image.csv.CSVComparer;

class CompareHelper {

    /** 
     * <p>Toggles a behaviour that replaces reference files with newly created onces in certain circumstances.
     * 
     * <p>If true, and an {@link #assertIdentical} fails, the first path in the comparer
     * (the newly created file) will be copied on top of the second path in the comparer
     * (the reference file).
     * 
     * <p>If false, no copying occurs.
     * 
     * <p>This is a powerful tool to update resources when they are out of sync with
     * the tests, but should be used very <b>carefully</b> as it overrides existing
     * files in the source-code directory.
     */
    private static final boolean COPY_NOT_IDENTICAL = false;
    
    private static final CSVComparer CSV_COMPARER = new CSVComparer(",", true, 0, true, false);

    public static void compareOutputWithSaved(
            Path pathAbsoluteOutput, String pathRelativeSaved, String[] relativePaths)
            throws OperationFailedException {

        DualComparer comparer =
                DualComparerFactory.compareExplicitFolderToTest(
                        pathAbsoluteOutput, pathRelativeSaved);

        for (String path : relativePaths) {
            assertIdentical(comparer, path);
        }
    }

    @SuppressWarnings("unused")
    private static void assertIdentical(DualComparer comparer, String relativePath)
            throws OperationFailedException {
        boolean identical = compareForExtra(comparer, relativePath);
        
        if (COPY_NOT_IDENTICAL && !identical) {
            try {
                comparer.copyFromPath1ToPath2(relativePath);
            } catch (IOException e) {
                throw new OperationFailedException(e);
            }
        } else {
            assertTrue(relativePath + " is not identical", identical);
        }
    }

    private static boolean compareForExtra(DualComparer comparer, String relativePath)
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
