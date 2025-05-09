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

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.List;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.io.input.file.FileNamerContext;
import org.anchoranalysis.io.input.file.NamedFile;
import org.anchoranalysis.plugin.io.bean.file.namer.patternspan.PatternSpan;
import org.anchoranalysis.test.LoggerFixture;
import org.junit.jupiter.api.Test;

class PatternSpanTest {

    private static final Logger LOGGER = LoggerFixture.suppressedLogger();

    @Test
    void testSimple() {

        String[] inputs = {"/a/b/c.txt", "/a/d/c.txt", "/a/e/c.txt"};

        String[] expected = {"b", "d", "e"};
        applyTest(inputs, expected);
    }

    @Test
    void testPaths() {
        String[] inputs = {
            "D:/Users/owen/Pictures/To Integrate/Feb 2020/P1210940.JPG",
            "D:/Users/owen/Pictures/To Integrate/Feb 2020/Klosters (Feb 2020)/P1210904.JPG"
        };

        String[] expected = {"P1210940", "Klosters (Feb 2020)/P1210904"};

        applyTest(inputs, expected);
    }

    @Test
    void testEmptyString() {
        String[] inputs = {
            "D:/Users/owen/Pictures/To Integrate/Feb 2020/P1210940.JPG",
            "D:/Users/owen/Pictures/To Integrate/Feb 2020/P1210940.JPG.TXT"
        };

        String[] expected = {"P1210940.JPG", "P1210940.JPG.TXT"};

        applyTest(inputs, expected);
    }

    // When there is no extension, the right-side should be kept
    @Test
    void testWithoutExtension() {

        String[] inputs = {"/a/b/c", "/a/d/c", "/a/e/c"};

        String[] expected = {"b/c", "d/c", "e/c"};
        applyTest(inputs, expected);
    }

    private static void applyTest(String[] paths, String[] expected) {
        List<File> files = filesFromStrs(paths);

        PatternSpan span = new PatternSpan();
        List<NamedFile> derived = span.deriveName(files, new FileNamerContext(LOGGER));

        for (int i = 0; i < expected.length; i++) {
            assertIndexEquals(derived, i, expected[i]);
        }
    }

    private static List<File> filesFromStrs(String[] paths) {
        return FunctionalList.mapToList(paths, File::new);
    }

    private static void assertIndexEquals(List<NamedFile> ret, int index, String expected) {
        assertEquals(expected, ret.get(index).getIdentifier());
    }
}
