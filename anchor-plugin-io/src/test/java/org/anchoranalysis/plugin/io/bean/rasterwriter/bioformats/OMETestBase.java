package org.anchoranalysis.plugin.io.bean.rasterwriter.bioformats;

import java.io.IOException;
import java.util.Optional;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.rasterwriter.RasterWriter;
import org.anchoranalysis.io.bioformats.ConfigureBioformatsLogging;
import org.anchoranalysis.test.image.rasterwriter.RasterWriterTestBase;
import org.junit.Test;

/**
 * Base class for {@link RasterWriter}s that output the OME file formats.
 * 
 * @author Owen Feehan
 *
 */
public abstract class OMETestBase extends RasterWriterTestBase {

    static {
        ConfigureBioformatsLogging.instance().makeSureConfigured();
    }
        
    /**
     * Creates for a particular extension and types of comparison.
     *  
     * @param extension the extension (without a leading period).
     * @param bytewiseCompare iff true, a bytewise comparison occurs between the saved-file and the newly created file.
     * @param extensionVoxelwiseCompare iff defined, a voxel-wise comparison occurs with the saved-rasters from a different extension.
     */
    public OMETestBase(String extension, boolean bytewiseCompare, Optional<String> extensionVoxelwiseCompare) {
        super(extension, true, bytewiseCompare, extensionVoxelwiseCompare);
    }
    
    @Test
    public void testSingleChannel() throws RasterIOException, IOException {
        tester.testSingleChannel();
    }
    
    @Test
    public void testTwoChannels() throws RasterIOException, IOException {
        tester.testTwoChannels();
    }
    
    @Test
    public void testThreeChannelsSeparate() throws RasterIOException, IOException {
        tester.testThreeChannelsSeparate();
    }
    
    @Test
    public void testThreeChannelsRGB() throws RasterIOException, IOException {
        tester.testThreeChannelsRGB();
    }
    
    @Test
    public void testFourChannels() throws RasterIOException, IOException {
        tester.testFourChannels();
    }
}
