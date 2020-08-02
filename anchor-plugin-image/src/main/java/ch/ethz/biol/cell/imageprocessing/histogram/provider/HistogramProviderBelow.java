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

package ch.ethz.biol.cell.imageprocessing.histogram.provider;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.HistogramProviderOne;
import org.anchoranalysis.image.histogram.Histogram;

/**
 * This cuts a Histogram below a threshold BUT TRANSFERS ALL THE COUNT above the threshold into the
 * just below the threshold value
 *
 * <p>Note that this is NOT SYMMETRIC behaviour with HistogramProviderAbove
 *
 * @author Owen Feehan
 */
public class HistogramProviderBelow extends HistogramProviderOne {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private int threshold = 0;

    @BeanField @Getter @Setter private boolean merge = false;
    // END BEAN PROPERTIES

    @Override
    public Histogram createFromHistogram(Histogram h) throws CreateException {

        for (int i = threshold; i <= h.getMaxBin(); i++) {
            h.transferVal(i, threshold - 1);
        }
        return h;
    }
}
