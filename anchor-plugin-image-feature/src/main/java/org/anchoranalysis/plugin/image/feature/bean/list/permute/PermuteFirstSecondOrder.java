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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.permute.property.PermutePropertySequenceInteger;
import org.anchoranalysis.bean.primitive.StringSet;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.input.FeatureInputDictionary;
import org.anchoranalysis.plugin.operator.feature.bean.arithmetic.MultiplyByConstant;
import org.anchoranalysis.plugin.operator.feature.bean.range.IfOutsideRange;
import org.anchoranalysis.plugin.operator.feature.bean.statistics.StatisticalBase;

/**
 * Permutes a feature by applying first and second order statistical operations.
 *
 * @param <T> the type of feature input
 */
@RequiredArgsConstructor
public abstract class PermuteFirstSecondOrder<T extends FeatureInputDictionary>
        extends PermuteFeatureSequenceInteger<T> {

    /** If true, the constant is appended to the param prefix (a dot and a number). */
    @BeanField @Getter @Setter private boolean paramPrefixAppendNumber = true;

    /** Factory for creating first and second order statistical operations. */
    private final CreateFirstSecondOrder<T> factory;

    /** Minimum value for the range. */
    private final double minRange;

    /** Maximum value for the range. */
    private final double maxRange;

    /**
     * Functional interface for creating first and second order statistical operations.
     *
     * @param <T> the type of feature input
     */
    @FunctionalInterface
    public static interface CreateFirstSecondOrder<T extends FeatureInput> {
        /**
         * Creates a new {@link StatisticalBase} instance.
         *
         * @return a new {@link StatisticalBase} instance
         */
        StatisticalBase<T> create();
    }

    @Override
    protected PermuteFeature<Integer, T> createDelegate(Feature<T> feature)
            throws ProvisionFailedException {

        PermuteFeature<Integer, T> delegate = new PermuteFeature<>();

        // Wrap our feature in a gaussian score
        Feature<T> featureScore = feature.duplicateBean();
        featureScore = wrapInScore(featureScore);
        featureScore = wrapWithMultiplyByConstant(featureScore);
        featureScore = wrapWithMinMaxRange(featureScore);
        delegate.setFeature(featureScore);

        return delegate;
    }

    /**
     * Wraps the feature with a multiply by constant operation.
     *
     * @param feature the feature to wrap
     * @return the wrapped feature
     */
    private Feature<T> wrapWithMultiplyByConstant(Feature<T> feature) {
        MultiplyByConstant<T> out = new MultiplyByConstant<>();
        out.setItem(feature);
        out.setValue(1);
        return out;
    }

    /**
     * Wraps the feature with a min-max range operation.
     *
     * @param feature the feature to wrap
     * @return the wrapped feature
     */
    private Feature<T> wrapWithMinMaxRange(Feature<T> feature) {
        IfOutsideRange<T> out = new IfOutsideRange<>();
        out.setItem(feature);
        out.setMin(minRange);
        out.setMax(maxRange);
        out.setBelowMinValue(minRange);
        out.setAboveMaxValue(maxRange);
        return out;
    }

    /**
     * Wraps the feature with a statistical score operation.
     *
     * @param feature the feature to wrap
     * @return the wrapped feature
     */
    private Feature<T> wrapInScore(Feature<T> feature) {
        StatisticalBase<T> featureScore = factory.create();
        featureScore.setItem(feature);
        featureScore.setItemMean(createParam("_fitted_normal_mean", paramPrefixAppendNumber));
        featureScore.setItemStdDev(createParam("_fitted_normal_sd", paramPrefixAppendNumber));
        return featureScore;
    }

    @Override
    protected PermutePropertySequenceInteger configurePermuteProperty(
            PermutePropertySequenceInteger permuteProperty) {
        if (permuteProperty.getAdditionalPropertyPaths() == null) {
            permuteProperty.setAdditionalPropertyPaths(new StringSet());
        }

        if (paramPrefixAppendNumber) {
            permuteProperty.getAdditionalPropertyPaths().add("item.item.itemMean.key");
            permuteProperty.getAdditionalPropertyPaths().add("item.item.itemStdDev.key");
        }

        permuteProperty.setPropertyPath(
                String.format("item.item.item.%s", permuteProperty.getPropertyPath()));
        return permuteProperty;
    }
}
