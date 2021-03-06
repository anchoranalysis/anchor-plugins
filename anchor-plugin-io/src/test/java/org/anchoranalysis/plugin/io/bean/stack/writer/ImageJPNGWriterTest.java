package org.anchoranalysis.plugin.io.bean.stack.writer;

import org.anchoranalysis.image.io.bean.stack.writer.StackWriter;
import org.anchoranalysis.io.imagej.bean.stack.writer.PNG;
import org.anchoranalysis.test.image.rasterwriter.PNGTestBase;

/**
 * Tests {@link PNG}, the ImageJ PNG writer.
 * 
 * <p>This test is located here rather than in the same package as {@link PNG} so as to have
 * access to {@link PNGTestBase}.
 * 
 * @author Owen Feehan
 *
 */
class ImageJPNGWriterTest extends PNGTestBase {

    @Override
    protected StackWriter createWriter() {
        return new PNG();
    }
}
