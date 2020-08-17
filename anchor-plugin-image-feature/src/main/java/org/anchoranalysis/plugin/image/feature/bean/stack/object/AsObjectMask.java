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

package org.anchoranalysis.plugin.image.feature.bean.stack.object;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.ChildCacheName;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.image.feature.bean.stack.FeatureStack;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.feature.stack.FeatureInputStack;

/**
 * Treats a channel as an object-mask, assuming binary values of 0 and 255 and calls an object-mask
 * feature
 *
 * @author Owen Feehan
 */
public class AsObjectMask extends FeatureStack {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private Feature<FeatureInputSingleObject> item;

    /**
     * The channel that that forms an object-mask with the bounding-box set to entire channel's
     * dimensions
     */
    @BeanField @Getter @Setter private int nrgIndex = 0;
    // END BEAN PROPERTIES

    @Override
    public double calculate(SessionInput<FeatureInputStack> input) throws FeatureCalculationException {

        return input.forChild()
                .calculate(
                        item,
                        new CalculateDeriveObjectInput(nrgIndex),
                        new ChildCacheName(AsObjectMask.class, nrgIndex));
    }
}
