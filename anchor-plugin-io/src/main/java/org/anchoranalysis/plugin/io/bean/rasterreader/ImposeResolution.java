/*-
 * #%L
 * anchor-plugin-io
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
/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.rasterreader;

import java.nio.file.Path;
import java.util.Optional;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.rasterreader.RasterReader;
import org.anchoranalysis.image.io.rasterreader.OpenedRaster;

public class ImposeResolution extends RasterReader {

    // START BEAN PROPERTIES
    @BeanField private RasterReader rasterReader;

    @BeanField private double resX;

    @BeanField private double resY;

    @BeanField private double resZ = 0.0;

    /** Keep the z-resolution */
    @BeanField private boolean keepZ = false;
    // END BEAN PROPERTIES

    private class ImposeResolutionProcessor
            implements OpenedRasterAlterDimensions.ConsiderUpdatedImageRes {

        public ImposeResolutionProcessor() {
            super();
        }

        @Override
        public Optional<ImageResolution> maybeUpdatedResolution(ImageResolution res)
                throws RasterIOException {
            return Optional.of(new ImageResolution(resX, resY, keepZ ? res.getZ() : resZ));
        }
    }

    @Override
    public OpenedRaster openFile(Path filepath) throws RasterIOException {

        OpenedRaster delegate = rasterReader.openFile(filepath);

        return new OpenedRasterAlterDimensions(delegate, new ImposeResolutionProcessor());
    }

    public RasterReader getRasterReader() {
        return rasterReader;
    }

    public void setRasterReader(RasterReader rasterReader) {
        this.rasterReader = rasterReader;
    }

    public double getResX() {
        return resX;
    }

    public void setResX(double resX) {
        this.resX = resX;
    }

    public double getResY() {
        return resY;
    }

    public void setResY(double resY) {
        this.resY = resY;
    }

    public double getResZ() {
        return resZ;
    }

    public void setResZ(double resZ) {
        this.resZ = resZ;
    }

    public boolean isKeepZ() {
        return keepZ;
    }

    public void setKeepZ(boolean keepZ) {
        this.keepZ = keepZ;
    }
}
