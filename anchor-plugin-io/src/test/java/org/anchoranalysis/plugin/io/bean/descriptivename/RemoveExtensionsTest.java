/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.descriptivename;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.input.descriptivename.DescriptiveFile;
import org.anchoranalysis.test.LoggingFixture;
import org.junit.Test;

public class RemoveExtensionsTest {

    private static final Logger LOGGER = LoggingFixture.suppressedLogErrorReporter();

    @Test
    public void testPreserveExt() throws AnchorIOException {

        List<DescriptiveFile> df = applyTest(true, listOfFiles());

        assertEquals("a/b/c/d", nameFor(df, 0));
        assertEquals("a/b/c/e.txt", nameFor(df, 1));
        assertEquals("a/b/c/e.csv", nameFor(df, 2));
    }

    @Test(expected = AnchorIOException.class)
    public void testNonUnique() throws AnchorIOException {
        applyTest(false, listOfFiles());
    }

    @Test
    public void testDoubleExt() throws AnchorIOException {

        List<DescriptiveFile> df = applyTest(true, listOfFilesDoubleExt());

        assertEquals(".picasaoriginals/2010-01-28 04.41.38", nameFor(df, 0));
        assertEquals(".picasaoriginals/2010-01-28 04.41.38.1", nameFor(df, 1));
    }

    @Test
    public void testOneEmpty() throws AnchorIOException {

        List<DescriptiveFile> df = applyTest(true, listOfFilesOneEmpty());

        assertEquals("unknownName", nameFor(df, 0));
        assertEquals("Gray", nameFor(df, 1));
    }

    private List<DescriptiveFile> applyTest(boolean preserveExtension, List<File> files)
            throws AnchorIOException {
        RemoveExtensions re = new RemoveExtensions();

        // The normalize-path is a simple descriptive-name that reuses the existing
        //  path, only making sure it has forward-slashes. So it's good for testing
        //  RemoveExtensions in (almost) isolation.
        re.setDescriptiveName(new NormalizedPath());

        re.setPreserveExtensionIfDuplicate(preserveExtension);
        return re.descriptiveNamesForCheckUniqueness(files, LOGGER);
    }

    private static List<File> listOfFiles() {
        File file1 = new File("a/b/c/d.txt");
        File file2 = new File("a/b/c/e.txt");
        File file3 = new File("a/b/c/e.csv"); // like file2 but with different extension

        return Arrays.asList(file1, file2, file3);
    }

    private static List<File> listOfFilesDoubleExt() {
        File file1 = new File(".picasaoriginals/2010-01-28 04.41.38.jpg");
        File file2 = new File(".picasaoriginals/2010-01-28 04.41.38.1.jpg");

        return Arrays.asList(file1, file2);
    }

    private static List<File> listOfFilesOneEmpty() {
        File file1 = new File(".jpg");
        File file2 = new File("Gray.png");

        return Arrays.asList(file1, file2);
    }

    private static String nameFor(List<DescriptiveFile> df, int index) {
        return df.get(index).getDescriptiveName();
    }
}
