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

        // The defaultStackReader should never be called during testing, so we pass a null.
        return INSTANCE.openFile(path, null);
    }
    
    private static InferFromHeader createInstance() {
        List<HeaderFormat> formats = Arrays.asList( new JPEG(), new PNG());
        return new InferFromHeader(new AlwaysReject(), formats);
    }
}
