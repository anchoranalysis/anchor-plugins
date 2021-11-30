/*-
 * #%L
 * anchor-plugin-image
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
package org.anchoranalysis.plugin.image.bean.thumbnail.object;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.core.stack.DisplayStack;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.test.image.DualComparer;
import org.anchoranalysis.test.image.DualComparerFactory;
import org.anchoranalysis.test.image.WriteIntoDirectory;

/**
 * Helper to write thumbnails into a temporary directory, and compare against saved images in {@code
 * src/test/resources}.
 *
 * @author Owen Feehan
 */
class WriteThumbnailsIntoDirectory {

    private WriteIntoDirectory writer;

    /**
     * Create with a particular temporary-directory.
     *
     * @param temporaryDirectory the path to the temporary-directory.
     */
    public WriteThumbnailsIntoDirectory(Path temporaryDirectory) {
        writer = new WriteIntoDirectory(temporaryDirectory, false);
    }

    /**
     * Writes the thumbnails into the temporary folder.
     *
     * @throws OperationFailedException
     */
    public List<DisplayStack> writeThumbnails(List<DisplayStack> thumbnails)
            throws OperationFailedException {
        try {
            writer.writeList("thumbnails", thumbnails, true);
            return thumbnails;
        } catch (OutputWriteFailedException e) {
            throw new OperationFailedException(e);
        }
    }

    /**
     * Assert that the thumbnails written into the temporary-directory are identical to those stored
     * in the test resources.
     */
    public void assertWrittenThumbnailsIdenticalToResources(String relativeResourcesRoot) {
        DualComparer comparer =
                DualComparerFactory.compareTemporaryDirectoryToTest(
                        writer.getDirectory(), Optional.of("thumbnails"), relativeResourcesRoot);
        assertTrue(
                comparer.compareTwoSubdirectories("."), "thumbnails are identical to saved copy");
    }
}
