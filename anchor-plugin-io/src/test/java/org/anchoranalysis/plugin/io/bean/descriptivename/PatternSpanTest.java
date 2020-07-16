/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.descriptivename;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.input.descriptivename.DescriptiveFile;
import org.anchoranalysis.plugin.io.bean.descriptivename.patternspan.PatternSpan;
import org.anchoranalysis.test.LoggingFixture;
import org.junit.Test;

public class PatternSpanTest {

    private static final Logger LOGGER = LoggingFixture.suppressedLogErrorReporter();

    @Test
    public void testSimple() throws AnchorIOException {

        String inputs[] = {"/a/b/c.txt", "/a/d/c.txt", "/a/e/c.txt"};

        String expected[] = {"b", "d", "e"};
        applyTest(inputs, expected);
    }

    @Test
    public void testPaths() throws AnchorIOException {
        String inputs[] = {
            "D:/Users/owen/Pictures/To Integrate/Feb 2020/P1210940.JPG",
            "D:/Users/owen/Pictures/To Integrate/Feb 2020/Klosters (Feb 2020)/P1210904.JPG"
        };

        String expected[] = {"P1210940", "Klosters (Feb 2020)/P1210904"};

        applyTest(inputs, expected);
    }

    @Test
    public void testEmptyStr() throws AnchorIOException {
        String inputs[] = {
            "D:/Users/owen/Pictures/To Integrate/Feb 2020/P1210940.JPG",
            "D:/Users/owen/Pictures/To Integrate/Feb 2020/P1210940.JPG.TXT"
        };

        String expected[] = {"P1210940.JPG", "P1210940.JPG.TXT"};

        applyTest(inputs, expected);
    }

    // When there is no extension, the right-side should be kept
    @Test
    public void testWithoutExtension() throws AnchorIOException {

        String inputs[] = {"/a/b/c", "/a/d/c", "/a/e/c"};

        String expected[] = {"b/c", "d/c", "e/c"};
        applyTest(inputs, expected);
    }

    private static void applyTest(String[] paths, String[] expected) {
        List<File> files = filesFromStrs(paths);

        PatternSpan ps = new PatternSpan();
        List<DescriptiveFile> ret = ps.descriptiveNamesFor(files, "unknown", LOGGER);

        for (int i = 0; i < expected.length; i++) {
            assertIndexEquals(ret, i, expected[i]);
        }
    }

    private static List<File> filesFromStrs(String[] paths) {
        return FunctionalList.mapToList(paths, File::new);
    }

    private static void assertIndexEquals(List<DescriptiveFile> ret, int index, String expected) {
        assertEquals(expected, ret.get(index).getDescriptiveName());
    }
}
