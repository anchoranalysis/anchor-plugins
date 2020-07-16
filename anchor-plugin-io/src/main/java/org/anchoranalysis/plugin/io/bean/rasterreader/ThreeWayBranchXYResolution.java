/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.rasterreader;

import java.nio.file.Path;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.rasterreader.RasterReader;
import org.anchoranalysis.image.io.rasterreader.OpenedRaster;

/**
 * Takes the XY-resolution determined by rasterReaderInput. Partitions this into three ranges, based
 * on two thresholds.
 *
 * <p>{@literal LOW_RANGE <= thresholdLow < MIDDLE_RANGE < thresholdHigh <= HIGH_RANGE}
 *
 * <p>Then selects the corresponding rasterReader for further reading
 *
 * <p>Assumes X resolution and Y resolution are the same.
 *
 * @author Owen Feehan
 */
public class ThreeWayBranchXYResolution extends RasterReader {

    // START BEAN PROPERTIES
    @BeanField private RasterReader rasterReaderInput;

    @BeanField private RasterReader rasterReaderLow;

    @BeanField private RasterReader rasterReaderMiddle;

    @BeanField private RasterReader rasterReaderHigh;

    @BeanField private double thresholdLow;

    @BeanField private double thresholdHigh;
    // END BEAN PROPERTIES

    @Override
    public OpenedRaster openFile(Path filepath) throws RasterIOException {

        OpenedRaster orInput = rasterReaderInput.openFile(filepath);

        ImageDimensions sd = orInput.dim(0);

        if (Math.abs(sd.getRes().getX() - sd.getRes().getY()) > 1e-12) {
            throw new RasterIOException(
                    String.format(
                            "X-Res (%f) must be equal to Y-Res (%f).",
                            sd.getRes().getX(), sd.getRes().getY()));
        }

        double xyRes = sd.getRes().getX();

        if (xyRes < thresholdLow) {
            return rasterReaderLow.openFile(filepath);
        } else if (xyRes < thresholdHigh) {
            return rasterReaderMiddle.openFile(filepath);
        } else {
            return rasterReaderHigh.openFile(filepath);
        }
    }

    public RasterReader getRasterReaderInput() {
        return rasterReaderInput;
    }

    public void setRasterReaderInput(RasterReader rasterReaderInput) {
        this.rasterReaderInput = rasterReaderInput;
    }

    public RasterReader getRasterReaderLow() {
        return rasterReaderLow;
    }

    public void setRasterReaderLow(RasterReader rasterReaderLow) {
        this.rasterReaderLow = rasterReaderLow;
    }

    public RasterReader getRasterReaderMiddle() {
        return rasterReaderMiddle;
    }

    public void setRasterReaderMiddle(RasterReader rasterReaderMiddle) {
        this.rasterReaderMiddle = rasterReaderMiddle;
    }

    public RasterReader getRasterReaderHigh() {
        return rasterReaderHigh;
    }

    public void setRasterReaderHigh(RasterReader rasterReaderHigh) {
        this.rasterReaderHigh = rasterReaderHigh;
    }

    public double getThresholdLow() {
        return thresholdLow;
    }

    public void setThresholdLow(double thresholdLow) {
        this.thresholdLow = thresholdLow;
    }

    public double getThresholdHigh() {
        return thresholdHigh;
    }

    public void setThresholdHigh(double thresholdHigh) {
        this.thresholdHigh = thresholdHigh;
    }
}
