/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.table;

import java.util.List;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.error.BeanDuplicateException;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.bean.list.FeatureListFactory;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.list.NamedFeatureStore;
import org.anchoranalysis.feature.list.NamedFeatureStoreFactory;

/**
 * Duplicates and sets a custom-name on a list of features.
 *
 * @author Owen Feehan
 */
class FeatureListCustomNameHelper {

    private final NamedFeatureStoreFactory storeFactory;

    public FeatureListCustomNameHelper(NamedFeatureStoreFactory storeFactory) {
        this.storeFactory = storeFactory;
    }

    /**
     * Duplicates features and sets a custom-name based upon the named-bean
     *
     * @param <T> feature input-type
     * @param features the list of named-beans providing features
     * @return a simple feature-list of duplicated beans with derived custom-names set
     * @throws OperationFailedException
     */
    public <T extends FeatureInput> FeatureList<T> copyFeaturesCreateCustomName(
            List<NamedBean<FeatureListProvider<T>>> features) throws OperationFailedException {
        try {
            NamedFeatureStore<T> featuresNamed = storeFactory.createNamedFeatureList(features);
            return copyFeaturesCreateCustomName(featuresNamed);

        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }

    private static <T extends FeatureInput> FeatureList<T> copyFeaturesCreateCustomName(
            NamedFeatureStore<T> features) throws OperationFailedException {
        try {
            return FeatureListFactory.mapFrom(
                    features, ni -> ni.getValue().duplicateChangeName(ni.getName()));

        } catch (BeanDuplicateException e) {
            throw new OperationFailedException(e);
        }
    }
}
