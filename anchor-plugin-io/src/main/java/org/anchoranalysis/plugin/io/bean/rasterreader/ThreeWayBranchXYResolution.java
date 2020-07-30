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

package org.anchoranalysis.plugin.io.bean.rasterreader;

import java.nio.file.Path;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.rasterreader.RasterReader;
import org.anchoranalysis.image.io.rasterreader.OpenedRaster;
import lombok.Getter;
import lombok.Setter;

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
    @BeanField @Getter @Setter private RasterReader rasterReaderInput;

    @BeanField @Getter @Setter private RasterReader rasterReaderLow;

    @BeanField @Getter @Setter private RasterReader rasterReaderMiddle;

    @BeanField @Getter @Setter private RasterReader rasterReaderHigh;

    @BeanField @Getter @Setter private double thresholdLow;

    @BeanField @Getter @Setter private double thresholdHigh;
    // END BEAN PROPERTIES

    @Override
    public OpenedRaster openFile(Path filepath) throws RasterIOException {

        OpenedRaster orInput = rasterReaderInput.openFile(filepath);

        ImageDimensions sd = orInput.dimensionsForSeries(0);

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
}
