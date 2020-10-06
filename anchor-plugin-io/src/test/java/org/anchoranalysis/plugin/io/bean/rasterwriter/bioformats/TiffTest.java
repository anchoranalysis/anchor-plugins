package org.anchoranalysis.plugin.io.bean.rasterwriter.bioformats;

import org.anchoranalysis.image.io.bean.rasterwriter.RasterWriter;
import org.anchoranalysis.io.bioformats.ConfigureBioformatsLogging;
import org.anchoranalysis.test.image.rasterwriter.TiffTestBase;

/**
 * Tests writing TIFFs using the Bioformats plugin.
 * 
 * @author Owen Feehan
 *
 */
public class TiffTest extends TiffTestBase {

    static {
        ConfigureBioformatsLogging.instance().makeSureConfigured();
    }
    
    @Override
    protected RasterWriter createWriter() {
        return new Tiff();
    }

}
