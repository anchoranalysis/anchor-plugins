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
import org.anchoranalysis.bean.permute.property.PermutePropertySequenceInteger;
import org.anchoranalysis.bean.primitive.StringSet;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.input.FeatureInputDictionary;
import org.anchoranalysis.plugin.operator.feature.bean.arithmetic.Divide;

/**
 * Similar to FeatureListProviderPermute but embeds the feature in a GaussianScore
 *
 * @author Owen Feehan
 * @param <T> feature-input
 */
public class PermuteDivideByParam<T extends FeatureInputDictionary>
        extends PermuteFeatureSequenceInteger<T> {

    @Override
    protected PermuteFeature<Integer, T> createDelegate(Feature<T> feature)
            throws ProvisionFailedException {

        PermuteFeature<Integer, T> delegate = new PermuteFeature<>();

        // Wrap our feature in a Divide
        delegate.setFeature(wrapInDivide(feature.duplicateBean()));

        return delegate;
    }

    @Override
    protected PermutePropertySequenceInteger configurePermuteProperty(
            PermutePropertySequenceInteger permuteProperty) {
        if (permuteProperty.getAdditionalPropertyPaths() == null) {
            permuteProperty.setAdditionalPropertyPaths(new StringSet());
        }
        permuteProperty.setPropertyPath(
                String.format(
                        "list.%s",
                        permuteProperty
                                .getPropertyPath()) // This will change the first item in the list
                );
        return permuteProperty;
    }

    private Feature<T> wrapInDivide(Feature<T> feature) {
        Divide<T> featureScore = new Divide<>();
        featureScore.setList(Arrays.asList(feature, createParam("_median", false)));
        return featureScore;
    }
}
