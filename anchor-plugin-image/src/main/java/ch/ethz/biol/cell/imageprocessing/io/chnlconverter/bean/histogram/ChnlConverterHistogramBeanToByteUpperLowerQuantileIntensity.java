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

package ch.ethz.biol.cell.imageprocessing.io.chnlconverter.bean.histogram;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.bean.chnl.converter.ConvertChannelToWithHistogram;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.stack.region.chnlconverter.attached.ChnlConverterAttached;
import org.anchoranalysis.image.stack.region.chnlconverter.attached.histogram.ChnlConverterHistogramUpperLowerQuantileIntensity;
import lombok.Getter;
import lombok.Setter;

public class ChnlConverterHistogramBeanToByteUpperLowerQuantileIntensity
        extends ConvertChannelToWithHistogram {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private double quantileLower = 0.0;

    @BeanField @Getter @Setter private double quantileUpper = 1.0;

    /** Sets the min by multiplying the quantileLower by this constant */
    @BeanField @Getter @Setter private double scaleLower = 1.0;

    /** Sets the max by multiplying the quantileUpper by this constant */
    @BeanField @Getter @Setter private double scaleUpper = 1.0;
    // END BEAN PROPERTIES

    @Override
    public ChnlConverterAttached<Histogram, ?> createConverter() {
        return new ChnlConverterHistogramUpperLowerQuantileIntensity(
                quantileLower, quantileUpper, scaleLower, scaleUpper);
    }
}
