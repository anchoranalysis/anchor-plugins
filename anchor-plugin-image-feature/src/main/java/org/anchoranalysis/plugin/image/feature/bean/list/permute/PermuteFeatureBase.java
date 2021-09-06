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

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.SkipInit;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.input.FeatureInput;

/**
 * Creates permutations of a feature by modifying a particular property of it in different ways.
 *
 * <p>This class is designed to be an abstract-base-class for different types of permutations on a
 * single feature.
 *
 * @author Owen Feehan
 * @param <T> input-type of feature
 */
public abstract class PermuteFeatureBase<T extends FeatureInput> extends FeatureListProvider<T> {

    // START BEAN PROPERTIES
    @BeanField @SkipInit private Feature<T> feature;
    // END BEAN PROPERTIES

    @Override
    public FeatureList<T> get() throws ProvisionFailedException {
        return createPermutedFeaturesFor(feature);
    }

    protected abstract FeatureList<T> createPermutedFeaturesFor(Feature<T> feature)
            throws ProvisionFailedException;

    public Feature<T> getFeature() {
        return feature;
    }

    public void setFeature(Feature<T> feature) {
        this.feature = feature;
    }
}
