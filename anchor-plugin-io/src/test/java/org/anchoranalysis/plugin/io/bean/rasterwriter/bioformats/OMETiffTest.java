package org.anchoranalysis.plugin.io.bean.rasterwriter.bioformats;

import java.util.Optional;
import org.anchoranalysis.image.io.bean.rasterwriter.RasterWriter;

public class OMETiffTest extends OMETestBase {

    public OMETiffTest() {
        // Do not do a byewiseComparison as the OmeTIFF files contain differences
        //  each time a new file is produced. Unsure why, perhaps connected with UUIDs?
        super("ome.tif", false, Optional.of("ome.xml"));
    }

    @Override
    protected RasterWriter createWriter() {
        return new OMETiff();
    }
}
