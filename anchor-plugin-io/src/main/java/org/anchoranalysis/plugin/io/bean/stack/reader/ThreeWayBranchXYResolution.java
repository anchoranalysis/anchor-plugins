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

package org.anchoranalysis.plugin.io.bean.stack.reader;

import java.nio.file.Path;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.dimensions.Resolution;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.stack.StackReader;
import org.anchoranalysis.image.io.stack.OpenedRaster;

/**
 * Takes the XY-resolution determined by stackReaderInput. Partitions this into three ranges, based
 * on two thresholds.
 *
 * <p>{@literal LOW_RANGE <= thresholdLow < MIDDLE_RANGE < thresholdHigh <= HIGH_RANGE}
 *
 * <p>Then selects the corresponding stackReader for further reading
 *
 * <p>Assumes X resolution and Y resolution are the same.
 *
 * @author Owen Feehan
 */
public class ThreeWayBranchXYResolution extends StackReader {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private StackReader stackReaderInput;

    @BeanField @Getter @Setter private StackReader stackReaderLow;

    @BeanField @Getter @Setter private StackReader stackReaderMiddle;

    @BeanField @Getter @Setter private StackReader stackReaderHigh;

    @BeanField @Getter @Setter private double thresholdLow;

    @BeanField @Getter @Setter private double thresholdHigh;
    // END BEAN PROPERTIES

    @Override
    public OpenedRaster openFile(Path path) throws ImageIOException {

        Dimensions dimensions = dimensionsForPath(path);

        if (dimensions.resolution().isPresent()) {
            return openWithResolution(path, dimensions.resolution().get()); // NOSONAR
        } else {
            throw new ImageIOException(
                    "No image-resolution is present, so cannot perform this check.");
        }
    }
    
    private Dimensions dimensionsForPath(Path path) throws ImageIOException {
        try (OpenedRaster openedRaster = stackReaderInput.openFile(path)) {
            return openedRaster.dimensionsForSeries(0);
        }
    }

    private OpenedRaster openWithResolution(Path path, Resolution resolution)
            throws ImageIOException {
        if (Math.abs(resolution.x() - resolution.y()) > 1e-12) {
            throw new ImageIOException(
                    String.format(
                            "X-Res (%f) must be equal to Y-Res (%f).",
                            resolution.x(), resolution.y()));
        }

        double xyRes = resolution.x();

        if (xyRes < thresholdLow) {
            return stackReaderLow.openFile(path);
        } else if (xyRes < thresholdHigh) {
            return stackReaderMiddle.openFile(path);
        } else {
            return stackReaderHigh.openFile(path);
        }
    }
}
