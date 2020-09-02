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
import lombok.Setter;
import org.anchoranalysis.bean.StringSet;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.permute.property.PermutePropertySequenceInteger;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.input.FeatureInputParams;
import org.anchoranalysis.plugin.operator.feature.bean.arithmetic.MultiplyByConstant;
import org.anchoranalysis.plugin.operator.feature.bean.range.IfOutsideRange;
import org.anchoranalysis.plugin.operator.feature.bean.score.StatisticalScoreBase;

public abstract class PermuteFirstSecondOrder<T extends FeatureInputParams>
        extends PermuteFeatureSequenceInteger<T> {

    // START BEAN PROPERTIES
    /** If true the constant is appended to the param prefix (a dot and a number) */
    @BeanField @Getter @Setter private boolean paramPrefixAppendNumber = true;
    // END BEAN PROPERTIES

    private CreateFirstSecondOrder<T> factory;
    private double minRange;
    private double maxRange;

    @FunctionalInterface
    public static interface CreateFirstSecondOrder<T extends FeatureInput> {
        StatisticalScoreBase<T> create();
    }

    public PermuteFirstSecondOrder(
            CreateFirstSecondOrder<T> factory, double minRange, double maxRange) {
        super();
        this.factory = factory;
        this.minRange = minRange;
        this.maxRange = maxRange;
    }

    @Override
    protected PermuteFeature<Integer, T> createDelegate(Feature<T> feature) throws CreateException {

        PermuteFeature<Integer, T> delegate = new PermuteFeature<>();

        // Wrap our feature in a gaussian score
        Feature<T> featureScore = feature.duplicateBean();
        featureScore = wrapInScore(featureScore);
        featureScore = wrapWithMultiplyByConstant(featureScore);
        featureScore = wrapWithMinMaxRange(featureScore);
        delegate.setFeature(featureScore);

        return delegate;
    }

    private Feature<T> wrapWithMultiplyByConstant(Feature<T> feature) {
        MultiplyByConstant<T> out = new MultiplyByConstant<>();
        out.setItem(feature);
        out.setValue(1);
        return out;
    }

    private Feature<T> wrapWithMinMaxRange(Feature<T> feature) {
        IfOutsideRange<T> out = new IfOutsideRange<>();
        out.setItem(feature);
        out.setMin(minRange);
        out.setMax(maxRange);
        out.setBelowMinValue(minRange);
        out.setAboveMaxValue(maxRange);
        return out;
    }

    private Feature<T> wrapInScore(Feature<T> feature) {
        StatisticalScoreBase<T> featureScore = factory.create();
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
