/*-
 * #%L
 * anchor-plugin-image
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

package org.anchoranalysis.plugin.image.bean.scale;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.bean.spatial.ScaleCalculator;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.dimensions.Resolution;
import org.anchoranalysis.spatial.scale.ScaleFactor;

public class MinimumXYResolution extends ScaleCalculator {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private double minResolution = 10e-9;
    // STOP BEAN PROPERTIES

    @Override
    public ScaleFactor calculate(Optional<Dimensions> dimensionsToBeScaled)
            throws OperationFailedException {

        Resolution resolution =
                dimensionsToBeScaled
                        .flatMap(Dimensions::resolution)
                        .orElseThrow(
                                () -> new OperationFailedException("No source resolution exists"));

        // If there is no resolution information we cannot scale
        if (resolution.x() == 0 || resolution.y() == 0) {
            throw new OperationFailedException(
                    "Channel has zero x or y resolution. Cannot scale to min res.");
        }

        int x = ratio(resolution.x(), minResolution);
        int y = ratio(resolution.y(), minResolution);

        if (x < 0) {
            throw new OperationFailedException(
                    String.format(
                            "Insufficient resolution (%E). %E is required",
                            resolution.x(), minResolution));
        }

        if (y < 0) {
            throw new OperationFailedException(
                    String.format(
                            "Insufficient resolution (%E). %E is required",
                            resolution.y(), minResolution));
        }

        double xScaleDownRatio = twoToMinusPower(x);
        double yScaleDownRatio = twoToMinusPower(y);

        getLogger()
                .messageLogger()
                .logFormatted(
                        "Downscaling by factor %d,%d (mult by %f,%f)",
                        x, y, xScaleDownRatio, yScaleDownRatio);

        return new ScaleFactor(xScaleDownRatio, yScaleDownRatio);
    }

    // Returns x i.e. how much to downside where the factor is 2^x
    private static int ratio(double cnrtRes, double minRes) {

        double ratio = minRes / cnrtRes;

        double ratioLog2 = Math.log10(ratio) / Math.log10(2);

        if (ratioLog2 > 0) {
            return (int) Math.floor(ratioLog2);
        } else {
            return (int) Math.ceil(ratioLog2);
        }
    }

    private static double twoToMinusPower(int power) {
        return Math.pow(2.0, -1.0 * power);
    }
}