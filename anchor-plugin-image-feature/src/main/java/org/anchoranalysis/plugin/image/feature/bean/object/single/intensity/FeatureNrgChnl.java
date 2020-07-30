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

package org.anchoranalysis.plugin.image.feature.bean.object.single.intensity;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalculationException;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;

/**
 * A feature that uses a channel from the NRG-stack as identified by an index
 *
 * @author Owen Feehan
 */
public abstract class FeatureNrgChnl extends FeatureSingleObject {

    // START BEAN PROPERTIES
    /** Index of channel to use in the nrg-stack (0 is the first channel, 1 is second etc.) */
    @BeanField @Getter @Setter private int nrgIndex = 0;
    // END BEAN PROPERTIES

    @Override
    public double calc(SessionInput<FeatureInputSingleObject> input) throws FeatureCalculationException {

        Channel chnl = input.get().getNrgStackRequired().getChannel(nrgIndex);
        return calcForChnl(input, chnl);
    }

    protected abstract double calcForChnl(
            SessionInput<FeatureInputSingleObject> input, Channel chnl) throws FeatureCalculationException;
}
