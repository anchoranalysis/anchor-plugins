package org.anchoranalysis.plugin.io.bean.rasterwriter.bioformats;

import java.io.IOException;
import java.util.Optional;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.rasterwriter.RasterWriter;
import org.anchoranalysis.image.voxel.datatype.UnsignedByteVoxelType;
import org.anchoranalysis.image.voxel.datatype.UnsignedShortVoxelType;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;
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

    private static final VoxelDataType[] SUPPORTED_VOXEL_TYPES = RasterWriterTestBase.ALL_SUPPORTED_VOXEL_TYPES;
    
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
        tester.testSingleChannel(SUPPORTED_VOXEL_TYPES);
    }
    
    @Test
    public void testTwoChannels() throws RasterIOException, IOException {
        tester.testTwoChannels(SUPPORTED_VOXEL_TYPES);
    }
    
    @Test
    public void testThreeChannelsSeparate() throws RasterIOException, IOException {
        tester.testThreeChannelsSeparate(SUPPORTED_VOXEL_TYPES);
    }
    
    @Test
    public void testThreeChannelsRGBUnsignedByte() throws RasterIOException, IOException {
        tester.testThreeChannelsRGB(UnsignedByteVoxelType.INSTANCE);
    }
    
    @Test
    public void testThreeChannelsRGBUnsignedShort() throws RasterIOException, IOException {
        tester.testThreeChannelsRGB(UnsignedShortVoxelType.INSTANCE);
    }
    
    @Test
    public void testFourChannels() throws RasterIOException, IOException {
        tester.testFourChannels(SUPPORTED_VOXEL_TYPES);
    }
}
