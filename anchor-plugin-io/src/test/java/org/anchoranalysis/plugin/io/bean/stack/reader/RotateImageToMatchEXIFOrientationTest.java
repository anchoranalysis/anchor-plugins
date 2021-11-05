package org.anchoranalysis.plugin.io.bean.stack.reader;

import static org.junit.Assert.assertEquals;

import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.stack.reader.StackReaderOrientationCorrection;
import org.anchoranalysis.image.io.stack.input.OpenedImageFile;
import org.anchoranalysis.io.bioformats.ConfigureBioformatsLogging;
import org.anchoranalysis.io.bioformats.bean.BioformatsReader;
import org.anchoranalysis.spatial.box.Extent;
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

    private OpenImageFileHelper loader = new OpenImageFileHelper("exif", createReader());

    @Test
    void testWithoutExif() throws ImageIOException {
        checkSizeAndCornerVoxel("exif_absent.jpg", 519, 389, 255);
    }

    @Test
    void testWithExifNoRotation() throws ImageIOException {
        checkSizeAndCornerVoxel("exif_present_no_rotation_needed.jpg", 519, 389, 105);
    }

    @Test
    void testWithExifRotationNeeded() throws ImageIOException {
        checkSizeAndCornerVoxel("exif_present_rotation_needed.jpg", 3888, 5184, 42);
    }

    /**
     * Opens the file, and checks that the size is as expected, and that the intensity of the corner
     * voxel is as expected.
     *
     * @param filename the filename to open.
     * @param expectedWidth the expected width of the image.
     * @param expectedHeight the expected height of the image.
     * @param expectedCornerIntensity expected intensity of the voxel in the corner (coordinate
     *     0,0,0).
     */
    private void checkSizeAndCornerVoxel(
            String filename, int expectedWidth, int expectedHeight, int expectedCornerIntensity)
            throws ImageIOException {
        Extent expectedExtent = new Extent(expectedWidth, expectedHeight, 1);
        OpenedImageFile openedImageFile = loader.openFile(filename);
        assertEquals(expectedExtent, openedImageFile.dimensionsForSeries(0).extent());

        int firstVoxel = openedImageFile.open().get(0).getChannel(0).extract().voxel(0, 0);
        assertEquals(expectedCornerIntensity, firstVoxel);
    }

    private static StackReaderOrientationCorrection createReader() {
        return new RotateImageToMatchEXIFOrientation(new BioformatsReader());
    }
}
