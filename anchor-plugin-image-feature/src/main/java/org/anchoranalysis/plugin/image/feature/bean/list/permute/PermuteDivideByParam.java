/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.list.permute;

import java.util.Arrays;
import org.anchoranalysis.bean.StringSet;
import org.anchoranalysis.bean.permute.property.PermutePropertySequenceInteger;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.input.FeatureInputParams;
import org.anchoranalysis.plugin.operator.feature.bean.arithmetic.Divide;

/**
 * Similar to FeatureListProviderPermute but embeds the feature in a GaussianScore
 *
 * @author Owen Feehan
 * @param T feature-input
 */
public class PermuteDivideByParam<T extends FeatureInputParams>
        extends PermuteFeatureSequenceInteger<T> {

    @Override
    protected PermuteFeature<Integer, T> createDelegate(Feature<T> feature) throws CreateException {

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
