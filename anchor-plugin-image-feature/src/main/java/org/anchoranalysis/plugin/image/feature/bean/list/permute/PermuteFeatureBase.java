/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.list.permute;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.SkipInit;
import org.anchoranalysis.core.error.CreateException;
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
    public FeatureList<T> create() throws CreateException {
        return createPermutedFeaturesFor(feature);
    }

    protected abstract FeatureList<T> createPermutedFeaturesFor(Feature<T> feature)
            throws CreateException;

    public Feature<T> getFeature() {
        return feature;
    }

    public void setFeature(Feature<T> feature) {
        this.feature = feature;
    }
}
