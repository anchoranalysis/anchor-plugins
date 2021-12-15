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
package org.anchoranalysis.plugin.io.bean.metadata.reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.anchoranalysis.image.core.stack.ImageMetadata;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.plugin.io.bean.metadata.header.HeaderFormat;
import org.anchoranalysis.plugin.io.bean.metadata.header.JPEG;
import org.anchoranalysis.plugin.io.bean.metadata.header.PNG;
import org.anchoranalysis.test.LoggingFixture;
import org.anchoranalysis.test.TestLoader;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link InferFromHeader}.
 *
 * @author Owen Feehan
 */
class InferFromHeaderTest {

    private static final InferFromHeader INSTANCE = createInstance();

    private TestLoader loader = TestLoader.createFromMavenWorkingDirectory();

    @Test
    void testJPG() throws ImageIOException {
        test("exif/exif_present_rotation_needed.jpg", 3888, 5184, 3);
    }

    @Test
    void testPNGWithExif() throws ImageIOException {
        test("exif/pngWithExif.png", 256, 256, 3);
    }

    @Test
    void testFlex() {
        testExpectRejection("exampleFormats/001001007.flex");
    }

    private void test(
            String filename, int expectedWidth, int expectedHeight, int expectedNumberChannels)
            throws ImageIOException {
        // The defaultStackReader should never be called during testing, so we pass a null.
        ImageMetadata metadata = loadMetadata(filename);
        assertEquals("image width", expectedWidth, metadata.getDimensions().x());
        assertEquals("image height", expectedHeight, metadata.getDimensions().y());
        assertEquals("number of channels", expectedNumberChannels, metadata.getNumberChannels());
    }

    private void testExpectRejection(String filename) {
        assertThrows(ImageIOException.class, () -> loadMetadata(filename));
    }

    private ImageMetadata loadMetadata(String filename) throws ImageIOException {
        Path path = loader.resolveTestPath(filename);

        // The defaultStackReader  should never be called during testing, so we pass a null,
        // even though this isn't allowed.
        return INSTANCE.openFile(path, null, LoggingFixture.suppressedOperationContext());
    }

    private static InferFromHeader createInstance() {
        List<HeaderFormat> formats = Arrays.asList(new JPEG(), new PNG());
        return new InferFromHeader(new AlwaysReject(), formats);
    }
}
