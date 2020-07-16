/*-
 * #%L
 * anchor-plugin-image-feature
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

package org.anchoranalysis.plugin.image.feature.bean.pixelscore;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.pixelwise.PixelScore;

public class PixelScoreDifference extends PixelScore {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private int nrgChnlIndexFirst = 0;

    @BeanField @Getter @Setter private int nrgChnlIndexSecond = 0;

    @BeanField @Getter @Setter private double width = 10;

    @BeanField @Getter @Setter private int minDifference = 0;
    // END BEAN PROPERTIES

    public static double calcDiffFromValue(
            int valFirst,
            int valSecond,
            double widthGreaterThan,
            double widthLessThan,
            int minDifference) {
        double diff = (double) (valFirst - valSecond - minDifference);

        if (diff < (-1 * widthLessThan)) {
            return 0.0;
        }

        if (diff > widthGreaterThan) {
            return 1.0;
        }

        if (diff < 0) {
            return (diff / (widthLessThan * 2)) + 0.5;
        } else {
            return (diff / (widthGreaterThan * 2)) + 0.5;
        }
    }

    public static double calcDiffFromValue(
            int valFirst, int valSecond, double width, int minDifference) {
        return calcDiffFromValue(valFirst, valSecond, width, width, minDifference);
    }

    public static double calcDiffFromParams(
            int[] pixelVals,
            int nrgChnlIndexFirst,
            int nrgChnlIndexSecond,
            double width,
            int minDifference) {
        return calcDiffFromValue(
                pixelVals[nrgChnlIndexFirst], pixelVals[nrgChnlIndexSecond], width, minDifference);
    }

    @Override
    public double calc(int[] pixelVals) throws FeatureCalcException {
        return calcDiffFromParams(
                pixelVals, nrgChnlIndexFirst, nrgChnlIndexSecond, width, minDifference);
    }
}
