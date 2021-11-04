package org.anchoranalysis.plugin.io.bean.stack.reader;

import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.util.Optional;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.channel.input.OrientationCorrectionNeeded;
import org.anchoranalysis.test.TestLoader;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link EXIFOrientationReader}.
 *
 * @author Owen Feehan
 */
class EXIFOrientationReaderTest {

    /**
     * The name of the subdirectory in {@code src/test/resources} where the image files are located.
     */
    private static final String SUBDIRECTORY_NAME = "exif";

    private TestLoader loader = TestLoader.createFromMavenWorkingDirectory();

    @Test
    void testWithoutExif() throws ImageIOException {
        test("exif_absent.jpg", Optional.empty());
    }

    @Test
    void testWithExifNoRotation() throws ImageIOException {
        test(
                "exif_present_no_rotation_needed.jpg",
                Optional.of(OrientationCorrectionNeeded.NO_ROTATION));
    }
    
    @Test
    void testWithExifRotation() throws ImageIOException {
        test(
                "exif_present_rotation_needed.jpg",
                Optional.of(OrientationCorrectionNeeded.ROTATE_90_CLOCKWISE));
    }

    private void test(String filename, Optional<OrientationCorrectionNeeded> expectedOrientation)
            throws ImageIOException {
        Path path = loader.resolveTestPath(SUBDIRECTORY_NAME + "/" + filename);
        assertEquals(expectedOrientation, EXIFOrientationReader.determineOrientationCorrection(path));
    }
}
