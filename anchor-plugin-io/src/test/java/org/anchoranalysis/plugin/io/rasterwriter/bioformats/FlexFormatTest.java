/* (C)2020 */
package org.anchoranalysis.plugin.io.rasterwriter.bioformats;

import static org.junit.Assert.*;

import java.nio.file.Path;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.rasterreader.OpenedRaster;
import org.anchoranalysis.io.bioformats.bean.BioformatsReader;
import org.anchoranalysis.test.TestLoader;
import org.junit.Test;

public class FlexFormatTest {

    private TestLoader loader = TestLoader.createFromMavenWorkingDirectory();

    /**
     * Tests the numChnls and numFrames from a known file, as it sometimes incorrectly reports as
     * numChnl==1 and numFrame==1, as opposed to numFrames==2 and numChnls==1 (which is what we
     * expect... but is itself incorrect
     *
     * <p>Note that this test MIGHT only work correctly when NOT run with the GPL bioformats
     * libraries on the class-path.
     *
     * <p>Otherwise the FlexReader will be used, and its exact behaviour has yet to be established.
     *
     * @throws RasterIOException
     */
    @Test
    public void testSizeCAndT() throws RasterIOException {

        Path path = loader.resolveTestPath("exampleFormats/001001007.flex");

        BioformatsReader bf = new BioformatsReader();
        OpenedRaster or = bf.openFile(path);

        assertTrue(or.numChnl() == 1);
        assertTrue(or.numSeries() == 1);
        assertTrue(or.numFrames() == 2);
    }
}
