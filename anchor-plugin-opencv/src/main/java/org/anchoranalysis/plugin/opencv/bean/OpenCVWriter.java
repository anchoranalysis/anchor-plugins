/*-
 * #%L
 * anchor-plugin-opencv
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

package org.anchoranalysis.plugin.opencv.bean;

import java.nio.file.Path;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.rasterwriter.RasterWriter;
import org.anchoranalysis.image.io.generator.raster.series.StackSeries;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.plugin.opencv.CVInit;
import org.anchoranalysis.plugin.opencv.convert.ConvertToMat;
import org.opencv.imgcodecs.Imgcodecs;

public class OpenCVWriter extends RasterWriter {

    static {
        CVInit.alwaysExecuteBeforeCallingLibrary();
    }

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private String extension = "png";
    // END BEAN PROPERTIES

    @Override
    public String defaultExtension() {
        return extension;
    }

    @Override
    public void writeTimeSeriesStackByte(StackSeries stackSeries, Path filePath, boolean makeRGB)
            throws RasterIOException {
        throw new RasterIOException("Writing time-series is unsupported for this format");
    }

    @Override
    public synchronized void writeStackByte(Stack stack, Path filePath, boolean makeRGB)
            throws RasterIOException {

        if (stack.getNumberChannels() == 3 && !makeRGB) {
            throw new RasterIOException("3-channel images can only be created as RGB");
        }

        try {
            Imgcodecs.imwrite(filePath.toString(), ConvertToMat.fromStack(stack));
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
