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

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.pixelwise.PixelScore;

public class PixelScoreRejectIfIntensityLessThan extends PixelScore {

    // START BEAN PROPERTIES
    @BeanField private PixelScore item;

    @BeanField private int nrgChnlIndex = 0;

    @BeanField private int minIntensity = 0;
    // END BEAN PROPERTIES

    @Override
    public double calc(int[] pixelVals) throws FeatureCalcException {

        int intensity = pixelVals[nrgChnlIndex];

        if (intensity < minIntensity) {
            return 0;
        }

        return item.calc(pixelVals);
    }

    public PixelScore getItem() {
        return item;
    }

    public void setItem(PixelScore item) {
        this.item = item;
    }

    public int getNrgChnlIndex() {
        return nrgChnlIndex;
    }

    public void setNrgChnlIndex(int nrgChnlIndex) {
        this.nrgChnlIndex = nrgChnlIndex;
    }

    public int getMinIntensity() {
        return minIntensity;
    }

    public void setMinIntensity(int minIntensity) {
        this.minIntensity = minIntensity;
    }
}
