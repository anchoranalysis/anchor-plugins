package org.anchoranalysis.plugin.io.bean.rasterwriter;

import org.anchoranalysis.image.io.bean.rasterwriter.RasterWriter;
import org.anchoranalysis.test.image.rasterwriter.PNGTestBase;

public class ImageIOWriterTest extends PNGTestBase {

    @Override
    protected RasterWriter createWriter() {
        return new ImageIOWriter();
    }
}
