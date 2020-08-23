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
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.bean.list.FeatureListProviderPrependName;
import org.anchoranalysis.feature.bean.operator.FeatureListElem;
import org.anchoranalysis.image.feature.bean.object.pair.First;
import org.anchoranalysis.image.feature.bean.object.pair.Merged;
import org.anchoranalysis.image.feature.bean.object.pair.Second;
import org.anchoranalysis.image.feature.object.input.FeatureInputPairObjects;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
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
     * For each feature in the list, a corresponding "aggregate" feature is created in the output
     * list
     */
    @BeanField @Getter @Setter private FeatureListProvider<FeatureInputSingleObject> item;

    @BeanField @Getter @Setter private String prependString;

    /** Method for reducing all pairs into a single value e.g. Mean, Max, Min etc. */
    @BeanField @Getter @Setter @SkipInit
    private FeatureListElem<FeatureInputPairObjects> reduce = new Mean<>();
    // END BEAN PROPERTIES

    @Override
    public FeatureList<FeatureInputPairObjects> create() throws CreateException {
        return item.create().map(this::createFeatureFor);
    }

    private Feature<FeatureInputPairObjects> createFeatureFor(
            Feature<FeatureInputSingleObject> featExst) {
        Feature<FeatureInputPairObjects> featOut =
                createAggregateFeature(
                        new First(featExst.duplicateBean()),
                        new Second(featExst.duplicateBean()),
                        new Merged(featExst.duplicateBean()));
        FeatureListProviderPrependName.setNewNameOnFeature(
                featOut, featExst.getFriendlyName(), prependString);
        return featOut;
    }

    protected abstract Feature<FeatureInputPairObjects> createAggregateFeature(
            Feature<FeatureInputPairObjects> first,
            Feature<FeatureInputPairObjects> second,
            Feature<FeatureInputPairObjects> merged);

    protected FeatureListElem<FeatureInputPairObjects> createReducedFeature(
            Feature<FeatureInputPairObjects> first, Feature<FeatureInputPairObjects> second) {
        FeatureListElem<FeatureInputPairObjects> featWithList =
                (FeatureListElem<FeatureInputPairObjects>) reduce.duplicateBean();
        featWithList.getList().add(first);
        featWithList.getList().add(second);
        return featWithList;
    }
}
