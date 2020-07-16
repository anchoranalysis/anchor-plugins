/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.list.pair;

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
 * @param T feature-input
 */
public abstract class FeatureListProviderAggregatePair
        extends FeatureListProvider<FeatureInputPairObjects> {

    // START BEAN PROPERTIES
    /**
     * For each feature in the list, a corresponding "aggregate" feature is created in the output
     * list
     */
    @BeanField private FeatureListProvider<FeatureInputSingleObject> item;

    @BeanField private String prependString;

    /** Method for reducing all pairs into a single value e.g. Mean, Max, Min etc. */
    @BeanField @SkipInit private FeatureListElem<FeatureInputPairObjects> reduce = new Mean<>();
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

    public String getPrependString() {
        return prependString;
    }

    public void setPrependString(String prependString) {
        this.prependString = prependString;
    }

    public FeatureListProvider<FeatureInputSingleObject> getItem() {
        return item;
    }

    public void setItem(FeatureListProvider<FeatureInputSingleObject> item) {
        this.item = item;
    }

    public FeatureListElem<FeatureInputPairObjects> getReduce() {
        return reduce;
    }

    public void setReduce(FeatureListElem<FeatureInputPairObjects> reduce) {
        this.reduce = reduce;
    }
}
