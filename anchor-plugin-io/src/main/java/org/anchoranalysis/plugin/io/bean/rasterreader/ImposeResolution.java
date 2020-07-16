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
