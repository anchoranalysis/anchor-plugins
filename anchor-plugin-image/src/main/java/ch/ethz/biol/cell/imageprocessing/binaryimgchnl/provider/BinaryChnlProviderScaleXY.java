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

package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProviderOne;
import org.anchoranalysis.image.bean.scale.ScaleCalculator;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.interpolator.Interpolator;
import org.anchoranalysis.image.interpolator.InterpolatorFactory;
import org.anchoranalysis.image.scale.ScaleFactor;

public class BinaryChnlProviderScaleXY extends BinaryChnlProviderOne {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ScaleCalculator scaleCalculator;

    @BeanField @Getter @Setter private boolean interpolate = true;
    // END BEAN PROPERTIES

    public static Mask scale(Mask chnl, ScaleCalculator scaleCalculator, Interpolator interpolator)
            throws CreateException {

        ScaleFactor scaleFactor;
        try {
            scaleFactor = scaleCalculator.calc(chnl.getDimensions());
        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }

        if (scaleFactor.isNoScale()) {
            // Nothing to do
            return chnl;
        }

        return chnl.scaleXY(scaleFactor, interpolator);
    }

    @Override
    public Mask createFromChnl(Mask chnl) throws CreateException {
        Interpolator interpolator =
                interpolate
                        ? InterpolatorFactory.getInstance().binaryResizing()
                        : InterpolatorFactory.getInstance().noInterpolation();
        return scale(chnl, scaleCalculator, interpolator);
    }
}
