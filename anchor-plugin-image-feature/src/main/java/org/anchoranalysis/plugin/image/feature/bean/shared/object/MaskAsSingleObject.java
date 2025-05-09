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

package org.anchoranalysis.plugin.image.feature.bean.shared.object;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.SkipInit;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.cache.CalculateForChild;
import org.anchoranalysis.feature.calculate.cache.ChildCacheName;
import org.anchoranalysis.feature.input.FeatureInputEnergy;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitialization;
import org.anchoranalysis.image.bean.provider.MaskProvider;
import org.anchoranalysis.image.core.mask.Mask;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;

/**
 * Calculates a feature, treating a mask as a single-object on the energy-stack.
 *
 * <p>This class extends {@link FeatureSingleObjectFromShared} to provide functionality for treating
 * a mask as a single object when calculating features.
 *
 * @author Owen Feehan
 * @param <T> feature-input type that extends {@link FeatureInputEnergy}
 */
public class MaskAsSingleObject<T extends FeatureInputEnergy>
        extends FeatureSingleObjectFromShared<T> {

    // START BEAN PROPERTIES
    /** The provider for the mask to be treated as a single object. */
    @BeanField @SkipInit @Getter @Setter private MaskProvider mask;

    // END BEAN PROPERTIES

    /** The created mask after initialization. */
    private Mask createdMask;

    @Override
    protected void beforeCalcWithInitialization(ImageInitialization initialization)
            throws InitializeException {
        mask.initializeRecursive(initialization, getLogger());

        try {
            createdMask = mask.get();
        } catch (ProvisionFailedException e) {
            throw new InitializeException(e);
        }
    }

    @Override
    protected double calc(
            CalculateForChild<T> calculateForChild,
            Feature<FeatureInputSingleObject> featureForSingleObject)
            throws FeatureCalculationException {
        return calculateForChild.calculate(
                featureForSingleObject,
                new CalculateMaskInput<>(createdMask),
                new ChildCacheName(MaskAsSingleObject.class, createdMask.hashCode()));
    }
}
