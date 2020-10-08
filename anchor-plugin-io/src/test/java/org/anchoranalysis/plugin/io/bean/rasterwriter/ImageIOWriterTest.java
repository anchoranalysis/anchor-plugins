package org.anchoranalysis.plugin.io.bean.rasterwriter;

import java.io.IOException;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.rasterwriter.RasterWriter;
import org.anchoranalysis.image.voxel.datatype.UnsignedShortVoxelType;
import org.anchoranalysis.test.image.rasterwriter.PNGTestBase;
import org.junit.Test;

public class ImageIOWriterTest extends PNGTestBase {

    @Override
    protected RasterWriter createWriter() {
        return new ImageIOWriter();
    }
    
    @Test(expected=RasterIOException.class)
    public void testThreeChannelsRGBUnsignedShort() throws RasterIOException, IOException {
        tester.testThreeChannelsRGB(UnsignedShortVoxelType.INSTANCE);
    }
}
