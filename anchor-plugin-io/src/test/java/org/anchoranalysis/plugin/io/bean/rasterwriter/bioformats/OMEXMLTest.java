package org.anchoranalysis.plugin.io.bean.rasterwriter.bioformats;

import java.util.Optional;
import org.anchoranalysis.image.io.bean.rasterwriter.RasterWriter;

public class OMEXMLTest extends OMETestBase {

    public OMEXMLTest() {
        super("ome.xml", true, Optional.empty());
    }

    @Override
    protected RasterWriter createWriter() {
        return new OMEXML();
    }
}
