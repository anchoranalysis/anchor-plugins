package org.anchoranalysis.plugin.opencv.bean.writer;

import java.io.IOException;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.rasterwriter.RasterWriter;
import org.anchoranalysis.image.voxel.datatype.UnsignedShortVoxelType;
import org.anchoranalysis.test.image.rasterwriter.PNGTestBase;
import org.junit.Test;

public class OpenCVWriterTest extends PNGTestBase {

    @Override
    protected RasterWriter createWriter() {
        return new OpenCVWriter();
    }
    
    @Test
    public void testThreeChannelsRGBUnsignedShort() throws RasterIOException, IOException {
        tester.testThreeChannelsRGB(UnsignedShortVoxelType.INSTANCE);
    }
}
