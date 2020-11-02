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
 * Tests that the RGB flag is set as expected.
 * @author Owen Feehan
 *
 */
public class OMEXMLRGBTest {

    @Test
    public void testOpen() {
        assertTrue( loadStack("ome.xml").isRgb() );
    }
    
    private Stack loadStack(String extension) {
        String identifier = IdentifierHelper.identiferFor(
                new ChannelSpecification(UnsignedByteVoxelType.INSTANCE, 3, true), false, StackTester.EXTENT_IDENTIFIER, false);
        
        TestLoaderImage loader = new TestLoaderImage( SavedFiles.createLoader(extension) );
        return loader.openStackFromTestPath( identifier + "." + extension);        
    }
}
