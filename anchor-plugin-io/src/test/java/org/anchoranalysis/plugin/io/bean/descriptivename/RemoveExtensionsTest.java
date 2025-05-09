/*-
 * #%L
 * anchor-plugin-io
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

package org.anchoranalysis.plugin.io.bean.descriptivename;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.io.input.file.FileNamerContext;
import org.anchoranalysis.io.input.file.NamedFile;
import org.anchoranalysis.plugin.io.bean.file.namer.NormalizedPath;
import org.anchoranalysis.plugin.io.bean.file.namer.RemoveExtension;
import org.anchoranalysis.test.LoggerFixture;
import org.junit.jupiter.api.Test;

class RemoveExtensionsTest {

    private static final Logger LOGGER = LoggerFixture.suppressedLogger();

    @Test
    void testPreserveExtension() throws InputReadFailedException {

        List<NamedFile> df = applyTest(true, listOfFiles());

        assertEquals("a/b/c/d", nameFor(df, 0));
        assertEquals("a/b/c/e.txt", nameFor(df, 1));
        assertEquals("a/b/c/e.csv", nameFor(df, 2));
    }

    @Test
    void testNonUnique() {
        assertThrows(InputReadFailedException.class, () -> applyTest(false, listOfFiles()));
    }

    @Test
    void testDoubleExtension() throws InputReadFailedException {

        List<NamedFile> df = applyTest(true, listOfFilesDoubleExtension());

        assertEquals(".picasaoriginals/2010-01-28 04.41.38", nameFor(df, 0));
        assertEquals(".picasaoriginals/2010-01-28 04.41.38.1", nameFor(df, 1));
    }

    @Test
    void testOneEmpty() throws InputReadFailedException {

        List<NamedFile> df = applyTest(true, listOfFilesOneEmpty());

        assertEquals("unknownName", nameFor(df, 0));
        assertEquals("Gray", nameFor(df, 1));
    }

    private List<NamedFile> applyTest(boolean preserveExtension, List<File> files)
            throws InputReadFailedException {
        RemoveExtension remove = new RemoveExtension();

        // The normalize-path is a simple descriptive-name that reuses the existing
        //  path, only making sure it has forward-slashes. So it's good for testing
        //  RemoveExtensions in (almost) isolation.
        remove.setNamer(new NormalizedPath());

        remove.setPreserveExtensionIfDuplicate(preserveExtension);
        return remove.deriveNameUnique(files, new FileNamerContext(LOGGER));
    }

    private static List<File> listOfFiles() {
        File file1 = new File("a/b/c/d.txt");
        File file2 = new File("a/b/c/e.txt");
        File file3 = new File("a/b/c/e.csv"); // like file2 but with different extension

        return Arrays.asList(file1, file2, file3);
    }

    private static List<File> listOfFilesDoubleExtension() {
        File file1 = new File(".picasaoriginals/2010-01-28 04.41.38.jpg");
        File file2 = new File(".picasaoriginals/2010-01-28 04.41.38.1.jpg");

        return Arrays.asList(file1, file2);
    }

    private static List<File> listOfFilesOneEmpty() {
        File file1 = new File(".jpg");
        File file2 = new File("Gray.png");

        return Arrays.asList(file1, file2);
    }

    private static String nameFor(List<NamedFile> df, int index) {
        return df.get(index).getIdentifier();
    }
}
