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

package org.anchoranalysis.plugin.image.feature.bean.list.pair;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.SkipInit;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.bean.operator.FeatureFromList;
import org.anchoranalysis.feature.name.AssignFeatureNameUtilities;
import org.anchoranalysis.image.feature.bean.object.pair.First;
import org.anchoranalysis.image.feature.bean.object.pair.Merged;
import org.anchoranalysis.image.feature.bean.object.pair.Second;
import org.anchoranalysis.image.feature.input.FeatureInputPairObjects;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.plugin.operator.feature.bean.list.Mean;

/**
 * Calculates features on each part of a pair (first, second, merged etc.) and then reduces the
 * calculation to a single number.
 *
 * <p>Specifically, each feature is calculated for the first, second, merged parts of the pair, then
 * "reduced" into a single feature-value.
 *
 * @author Owen Feehan
 */
public abstract class FeatureListProviderAggregatePair
        extends FeatureListProvider<FeatureInputPairObjects> {

    // START BEAN PROPERTIES
    /**
     * For each feature in the list, a corresponding <i>aggregate</i> feature is created in the
     * output list.
     */
    @BeanField @Getter @Setter private FeatureListProvider<FeatureInputSingleObject> item;

    /** The string to prepend. */
    @BeanField @Getter @Setter private String prefix;

    /** Method for reducing all pairs into a single value e.g. Mean, Max, Min etc. */
    @BeanField @Getter @Setter @SkipInit
    private FeatureFromList<FeatureInputPairObjects> reduce = new Mean<>();

    // END BEAN PROPERTIES

    @Override
    public FeatureList<FeatureInputPairObjects> get() throws ProvisionFailedException {
        return item.get().map(this::createFeatureFor);
    }

    /**
     * Creates an aggregate feature from individual features for first, second, and merged objects.
     *
     * @param first feature for the first object
     * @param second feature for the second object
     * @param merged feature for the merged object
     * @return an aggregate feature combining the three input features
     */
    protected abstract Feature<FeatureInputPairObjects> createAggregateFeature(
            Feature<FeatureInputPairObjects> first,
            Feature<FeatureInputPairObjects> second,
            Feature<FeatureInputPairObjects> merged);

    /**
     * Creates a reduced feature from features for the first and second objects.
     *
     * @param first feature for the first object
     * @param second feature for the second object
     * @return a {@link FeatureFromList} that reduces the two input features
     */
    protected FeatureFromList<FeatureInputPairObjects> createReducedFeature(
            Feature<FeatureInputPairObjects> first, Feature<FeatureInputPairObjects> second) {
        FeatureFromList<FeatureInputPairObjects> featureWithList =
                (FeatureFromList<FeatureInputPairObjects>) reduce.duplicateBean();
        featureWithList.getList().add(first);
        featureWithList.getList().add(second);
        return featureWithList;
    }

    /**
     * Creates a feature for pair objects based on a single object feature.
     *
     * @param existing the feature for single objects to base the new feature on
     * @return a new feature for pair objects
     */
    private Feature<FeatureInputPairObjects> createFeatureFor(
            Feature<FeatureInputSingleObject> existing) {
        Feature<FeatureInputPairObjects> out =
                createAggregateFeature(
                        new First(existing.duplicateBean()),
                        new Second(existing.duplicateBean()),
                        new Merged(existing.duplicateBean()));
        AssignFeatureNameUtilities.assignWithPrefix(out, existing.getFriendlyName(), prefix);
        return out;
    }
}
