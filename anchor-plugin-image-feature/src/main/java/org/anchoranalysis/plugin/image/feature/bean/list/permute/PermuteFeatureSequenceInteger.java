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
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.input.FeatureInputDictionary;
import org.anchoranalysis.plugin.operator.feature.bean.FromDictionary;

/**
 * Permutes a property on a feature with a sequence of integers.
 *
 * @author Owen Feehan
 * @param <T> feature-input type
 */
public abstract class PermuteFeatureSequenceInteger<T extends FeatureInputDictionary>
        extends PermuteFeatureBase<T> {

    /** Prefix for the parameter key. */
    @BeanField @Getter @Setter private String paramPrefix;

    /** Defines how to permute the property with a sequence of integers. */
    @BeanField @Getter @Setter private PermutePropertySequenceInteger permuteProperty;

    /**
     * Possible default instances for beans, saved from checkMisconfigured for delayed checks
     * elsewhere.
     */
    private BeanInstanceMap defaultInstances;

    @Override
    public void checkMisconfigured(BeanInstanceMap defaultInstances)
            throws BeanMisconfiguredException {
        super.checkMisconfigured(defaultInstances);
        this.defaultInstances = defaultInstances;
    }

    @Override
    protected FeatureList<T> createPermutedFeaturesFor(Feature<T> feature)
            throws ProvisionFailedException {
        PermuteFeature<Integer, T> delegate = createDelegate(feature);

        configurePermutePropertyOnDelegate(delegate);
        try {
            delegate.checkMisconfigured(defaultInstances);
        } catch (BeanMisconfiguredException e) {
            throw new ProvisionFailedException(e);
        }

        return delegate.get();
    }

    /**
     * Creates a delegate {@link PermuteFeature} for the given feature.
     *
     * @param feature the feature to permute
     * @return a new {@link PermuteFeature} instance
     * @throws ProvisionFailedException if the delegate cannot be created
     */
    protected abstract PermuteFeature<Integer, T> createDelegate(Feature<T> feature)
            throws ProvisionFailedException;

    /**
     * Configures the {@link PermutePropertySequenceInteger} for this permutation.
     *
     * @param permuteProperty the property to configure
     * @return the configured {@link PermutePropertySequenceInteger}
     */
    protected abstract PermutePropertySequenceInteger configurePermuteProperty(
            PermutePropertySequenceInteger permuteProperty);

    /**
     * Creates a parameter feature for the permutation.
     *
     * @param suffix the suffix for the parameter key
     * @param appendNumber whether to append a number to the key
     * @return a new {@link Feature} instance
     */
    protected Feature<T> createParam(String suffix, boolean appendNumber) {
        FromDictionary<T> param = new FromDictionary<>();
        param.setKeyPrefix(paramPrefix);
        param.setKey(sequenceNumberOrEmpty(appendNumber));
        param.setKeySuffix(suffix);
        return param;
    }

    /**
     * Returns the sequence number as a string if appendNumber is true, otherwise an empty string.
     *
     * @param appendNumber whether to append the number
     * @return the sequence number or an empty string
     */
    private String sequenceNumberOrEmpty(boolean appendNumber) {
        if (appendNumber) {
            return Integer.toString(permuteProperty.getSequence().getStart());
        } else {
            return "";
        }
    }

    /**
     * Configures the permute property on the delegate.
     *
     * @param delegate the delegate to configure
     */
    private void configurePermutePropertyOnDelegate(PermuteFeature<Integer, T> delegate) {
        PermutePropertySequenceInteger permutePropertyConfigured =
                configurePermuteProperty(
                        (PermutePropertySequenceInteger) permuteProperty.duplicateBean());

        delegate.setPermutations(Arrays.asList(permutePropertyConfigured));
    }
}
