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

package org.anchoranalysis.plugin.image.feature.bean.list.permute;

import java.util.Arrays;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.exception.BeanMisconfiguredException;
import org.anchoranalysis.bean.permute.property.PermutePropertySequenceInteger;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.input.FeatureInputParams;
import org.anchoranalysis.plugin.operator.feature.bean.Param;

/**
 * Permutes a property on a feature with a sequence of integers.
 *
 * @author Owen Feehan
 * @param <T> feature-input
 */
public abstract class PermuteFeatureSequenceInteger<T extends FeatureInputParams>
        extends PermuteFeatureBase<T> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private String paramPrefix;

    @BeanField @Getter @Setter private PermutePropertySequenceInteger permuteProperty;
    // END BEAN PROPERTIES

    // Possible defaultInstances for beans......... saved from checkMisconfigured for delayed checks
    // elsewhere
    private BeanInstanceMap defaultInstances;

    @Override
    public void checkMisconfigured(BeanInstanceMap defaultInstances)
            throws BeanMisconfiguredException {
        super.checkMisconfigured(defaultInstances);
        this.defaultInstances = defaultInstances;
    }

    @Override
    protected FeatureList<T> createPermutedFeaturesFor(Feature<T> feature) throws CreateException {
        PermuteFeature<Integer, T> delegate = createDelegate(feature);

        configurePermutePropertyOnDelegate(delegate);
        try {
            delegate.checkMisconfigured(defaultInstances);
        } catch (BeanMisconfiguredException e) {
            throw new CreateException(e);
        }

        return delegate.create();
    }

    protected abstract PermuteFeature<Integer, T> createDelegate(Feature<T> feature)
            throws CreateException;

    protected abstract PermutePropertySequenceInteger configurePermuteProperty(
            PermutePropertySequenceInteger permuteProperty);

    protected Feature<T> createParam(String suffix, boolean appendNumber) {
        Param<T> param = new Param<>();
        param.setKeyPrefix(paramPrefix);
        param.setKey(sequenceNumberOrEmpty(appendNumber));
        param.setKeySuffix(suffix);
        return param;
    }

    private String sequenceNumberOrEmpty(boolean appendNumber) {
        if (appendNumber) {
            return Integer.toString(permuteProperty.getSequence().getStart());
        } else {
            return "";
        }
    }

    private void configurePermutePropertyOnDelegate(PermuteFeature<Integer, T> delegate) {
        PermutePropertySequenceInteger permutePropertyConfigured =
                configurePermuteProperty(
                        (PermutePropertySequenceInteger) permuteProperty.duplicateBean());

        delegate.setPermutations(Arrays.asList(permutePropertyConfigured));
    }
}
