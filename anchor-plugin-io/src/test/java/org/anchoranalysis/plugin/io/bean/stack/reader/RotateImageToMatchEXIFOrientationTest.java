package org.anchoranalysis.plugin.io.bean.stack.reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.stack.reader.StackReaderOrientationCorrection;
import org.anchoranalysis.image.io.stack.input.OpenedImageFile;
import org.anchoranalysis.io.bioformats.ConfigureBioformatsLogging;
import org.anchoranalysis.io.bioformats.bean.BioformatsReader;
import org.anchoranalysis.spatial.box.Extent;
import org.anchoranalysis.test.LoggingFixture;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link RotateImageToMatchEXIFOrientation}.
 *
 * @author Owen Feehan
 */
public class RotateImageToMatchEXIFOrientationTest {

    static {
        ConfigureBioformatsLogging.instance().makeSureConfigured();
    }

    /** The tolerance allowed check checking if the intensity of two voxels are identical. */
    private static final int INTENSITY_TOLERANCE = 2;

    private OpenImageFileHelper loader = new OpenImageFileHelper("exif", createReader());

    @Test
    void testWithoutExif() throws ImageIOException {
        checkImage("exif_absent.jpg", 519, 389, 255);
    }

    @Test
    void testWithExifNoRotation() throws ImageIOException {
        checkImage("exif_present_no_rotation_needed.jpg", 519, 389, 105);
    }

    @Test
    void testWithExifRotationNeeded() throws ImageIOException {
        checkImage("exif_present_rotation_needed.jpg", 3888, 5184, 42);
    }

    @Test
    void testSeriesLandscape() throws ImageIOException {
        checkSeries("Landscape_", 1800, 1200, 111);
    }

    @Test
    void testSeriesPortrait() throws ImageIOException {
        checkSeries("Portrait_", 1200, 1800, 201);
    }

    /**
     * Like {@link #checkImage(String, int, int, int)} but each image in a series of 9 images.
     *
     * <p>To form the filename for each image in the series is formed from {@code fileNamePrefix +
     * index + ".jpg"}, where index is the corresponding number from 0 to 8 (inclusive).
     *
     * @param filenamePrefix the prefix for filename to open, to which an integer is appended.
     * @param expectedWidth the expected width of the image.
     * @param expectedHeight the expected height of the image.
     * @param expectedCornerIntensity expected intensity of the voxel in the corner (coordinate
     *     0,0,0).
     */
    private void checkSeries(
            String filenamePrefix,
            int expectedWidth,
            int expectedHeight,
            int expectedCornerIntensity)
            throws ImageIOException {
        for (int i = 0; i <= 8; i++) {
            checkImage(
                    filenamePrefix + i + ".jpg",
                    expectedWidth,
                    expectedHeight,
                    expectedCornerIntensity);
        }
    }

    /**
     * Opens the file, and checks that the size is as expected, and that the intensity of the corner
     * voxel is as expected.
     *
     * @param filename the filename to open.
     * @param expectedWidth the expected width of the image.
     * @param expectedHeight the expected height of the image.
     * @param expectedCornerIntensity expected intensity of the voxel in the corner (coordinate
     *     0,0,0). A tolerance of +- 2 is applied, as JPEGs may be encoded slightly differently.
     */
    private void checkImage(
            String filename, int expectedWidth, int expectedHeight, int expectedCornerIntensity)
            throws ImageIOException {
        Extent expectedExtent = new Extent(expectedWidth, expectedHeight, 1);
        OpenedImageFile openedImageFile = loader.openFile(filename, LoggingFixture.suppressedLogger());
        assertEquals(expectedExtent, openedImageFile.dimensionsForSeries(0).extent());

        int firstVoxel = openedImageFile.open().get(0).getChannel(0).extract().voxel(0, 0);
        assertTrue(Math.abs(expectedCornerIntensity - firstVoxel) <= INTENSITY_TOLERANCE);
    }

    private static StackReaderOrientationCorrection createReader() {
        return new RotateImageToMatchEXIFOrientation(new BioformatsReader());
    }
}
