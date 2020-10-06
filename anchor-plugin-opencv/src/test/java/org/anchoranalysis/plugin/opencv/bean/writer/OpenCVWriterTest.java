package org.anchoranalysis.plugin.opencv.bean.writer;

import org.anchoranalysis.image.io.bean.rasterwriter.RasterWriter;
import org.anchoranalysis.test.image.rasterwriter.PNGTestBase;

public class OpenCVWriterTest extends PNGTestBase {

    @Override
    protected RasterWriter createWriter() {
        return new OpenCVWriter();
    }
}
