package org.anchoranalysis.plugin.io.bean.rasterwriter.bioformats;

import static org.junit.Assert.assertTrue;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.voxel.datatype.UnsignedByteVoxelType;
import org.anchoranalysis.test.image.io.TestLoaderImage;
import org.anchoranalysis.test.image.rasterwriter.ChannelSpecification;
import org.anchoranalysis.test.image.rasterwriter.IdentifierHelper;
import org.anchoranalysis.test.image.rasterwriter.SavedFiles;
import org.anchoranalysis.test.image.rasterwriter.StackTester;
import org.junit.Test;

/**
 * Tests that the RGB flag is set as expected after loading from several different formats.
 * 
 * @author Owen Feehan
 *
 */
public class RGBOnStackTest {

    /** A 8-Bit RGB Stack. */
    private static ChannelSpecification CHANNEL_SPECIFICATION = new ChannelSpecification(UnsignedByteVoxelType.INSTANCE, 3, true);

    @Test
    public void testOMETiff() {
        assertForFormat("ome.tif");
    }
    
    @Test
    public void testTiff() {
        assertForFormat("tif");
    }
    
    @Test
    public void testPNG() {
        assertForFormat("png");
    }
    
    private void assertForFormat(String formatExtension) {
        assertTrue( loadStack(formatExtension).isRgb() );
    }
    
    private Stack loadStack(String extension) {
        String identifier = IdentifierHelper.identiferFor(
                CHANNEL_SPECIFICATION, false, StackTester.EXTENT_IDENTIFIER, false);
        
        TestLoaderImage loader = new TestLoaderImage( SavedFiles.createLoader(extension) );
        return loader.openStackFromTestPath( identifier + "." + extension);        
    }
}
