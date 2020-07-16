/* (C)2020 */
package org.anchoranalysis.plugin.opencv.bean;

import java.nio.file.Path;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.rasterwriter.RasterWriter;
import org.anchoranalysis.image.io.generator.raster.series.ImgStackSeries;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.plugin.opencv.CVInit;
import org.anchoranalysis.plugin.opencv.MatConverter;
import org.opencv.imgcodecs.Imgcodecs;

public class OpenCVPngWriter extends RasterWriter {

    static {
        CVInit.alwaysExecuteBeforeCallingLibrary();
    }

    @Override
    public String dfltExt() {
        return "png";
    }

    @Override
    public void writeTimeSeriesStackByte(ImgStackSeries stackSeries, Path filePath, boolean makeRGB)
            throws RasterIOException {
        throw new RasterIOException("Writing time-series is unsupported for this format");
    }

    @Override
    public synchronized void writeStackByte(Stack stack, Path filePath, boolean makeRGB)
            throws RasterIOException {

        if (stack.getNumChnl() == 3 && !makeRGB) {
            throw new RasterIOException("3-channel images can only be created as RGB");
        }

        try {
            Imgcodecs.imwrite(filePath.toString(), MatConverter.fromStack(stack));
        } catch (CreateException e) {
            throw new RasterIOException(e);
        }
    }

    @Override
    public void writeStackShort(Stack stack, Path filePath, boolean makeRGB)
            throws RasterIOException {
        writeStackByte(stack, filePath, makeRGB);
    }
}
