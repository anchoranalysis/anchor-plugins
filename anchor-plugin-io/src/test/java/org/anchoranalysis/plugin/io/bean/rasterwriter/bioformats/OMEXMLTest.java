package org.anchoranalysis.plugin.io.bean.rasterwriter.bioformats;

import java.io.IOException;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.rasterwriter.RasterWriter;
import org.anchoranalysis.io.bioformats.ConfigureBioformatsLogging;
import org.anchoranalysis.test.image.rasterwriter.RasterWriterTestBase;
import org.junit.Test;
import loci.formats.out.OMEXMLWriter;

public class OMEXMLTest extends RasterWriterTestBase {

    static {
        ConfigureBioformatsLogging.instance().makeSureConfigured();
    }
    
    public OMEXMLTest() {
        super("ome.xml", true);
    }

    @Override
    protected RasterWriter createWriter() {
        return new OMEXML();
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
