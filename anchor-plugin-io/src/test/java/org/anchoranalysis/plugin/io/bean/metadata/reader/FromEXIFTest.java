package org.anchoranalysis.plugin.io.bean.metadata.reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.nio.file.Path;
import org.anchoranalysis.image.core.stack.ImageMetadata;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.test.TestLoader;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link FromEXIF}.
 *
 * @author Owen Feehan
 */
class FromEXIFTest {

    private static final FromEXIF INSTANCE = new FromEXIF(new AlwaysReject());

    private TestLoader loader = TestLoader.createFromMavenWorkingDirectory();

    @Test
    void testJPG() throws ImageIOException {
        test("exif/exif_present_rotation_needed.jpg", 3888, 5184, 3);
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

        // The defaultStackReader should never be called during testing, so we pass a null.
        return INSTANCE.openFile(path, null);
    }
}
