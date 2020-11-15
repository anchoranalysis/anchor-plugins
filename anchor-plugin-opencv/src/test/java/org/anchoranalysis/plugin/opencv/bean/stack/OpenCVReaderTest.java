package org.anchoranalysis.plugin.opencv.bean.stack;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.voxel.datatype.UnsignedByteVoxelType;
import org.anchoranalysis.spatial.Extent;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.image.io.TestLoaderImage;
import org.junit.Test;

public class OpenCVReaderTest {

    private TestLoaderImage loader = new TestLoaderImage(TestLoader.createFromMavenWorkingDirectory(), new OpenCVReader() );
    
    private static final Extent EXPECTED_JAPAN_EXTENT = new Extent(3888,5184,1);
    
    @Test
    public void testJpegRGBNormalOrientation() {
        loadAndAssert("stackReader/japan_correct_orientation.jpg", EXPECTED_JAPAN_EXTENT);
    }
    
    @Test
    public void testJpegRGBAlternativeOrientation() {
        loadAndAssert("stackReader/japan_exif_alternative_orientation.jpg", EXPECTED_JAPAN_EXTENT);
    }
    
    private void loadAndAssert(String imageTestPath, Extent extent) {
        Stack stack = loader.openStackFromTestPath(imageTestPath);
        assertEquals("expected extent", extent, stack.extent());
        assertTrue("all channels identical type", stack.allChannelsHaveIdenticalType() );
        assertEquals("channel type", UnsignedByteVoxelType.INSTANCE, stack.getChannel(0).getVoxelDataType());
        assertTrue("rgb", stack.isRGB());        
    }
}
