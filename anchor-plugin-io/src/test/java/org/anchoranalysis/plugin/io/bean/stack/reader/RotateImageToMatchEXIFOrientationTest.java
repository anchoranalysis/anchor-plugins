package org.anchoranalysis.plugin.io.bean.stack.reader;

import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.stack.reader.StackReader;
import org.anchoranalysis.io.bioformats.ConfigureBioformatsLogging;
import org.anchoranalysis.io.bioformats.bean.BioformatsReader;
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

    /*@Test
    void testWithoutExif() throws ImageIOException {
        loader.openFile("exif_absent.jpg");
    }*/

    @Test
    void testWithExifNoRotation() throws ImageIOException {
        loader.openFile("exif_present_no_rotation_needed.jpg");
    }

    private static StackReader createReader() {
        return new RotateImageToMatchEXIFOrientation(new BioformatsReader());
    }
}
