package org.anchoranalysis.plugin.mpp.experiment.bean.feature.flexi;

import java.util.List;

import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.error.BeanDuplicateException;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.list.NamedFeatureStore;
import org.anchoranalysis.feature.list.NamedFeatureStoreFactory;

/**
 * Duplicates and sets a custom-name on a list of features.
 * 
 * @author Owen Feehan
 *
 */
class FeatureListCustomNameHelper {

	private FeatureListCustomNameHelper() {}
	
	/**
	 * Duplicates features and sets a custom-name based upon the named-bean
	 * 
	 * @param <T> feature input-type
	 * @param features the list of named-beans providing features
	 * @return a simple feature-list of duplicated beans with derived custom-names set
	 * @throws OperationFailedException
	 */
	public static <T extends FeatureInput> FeatureList<T> copyFeaturesCreateCustomName(
		List<NamedBean<FeatureListProvider<T>>> features
	) throws OperationFailedException {
		try {
			NamedFeatureStore<T> featuresNamed = NamedFeatureStoreFactory.createNamedFeatureList(features);
			return copyFeaturesCreateCustomName(featuresNamed);
			
		} catch (CreateException e) {
			throw new OperationFailedException(e);
		}
	}

	private static <T extends FeatureInput> FeatureList<T> copyFeaturesCreateCustomName( NamedFeatureStore<T> features ) throws OperationFailedException {
		
		FeatureList<T> out = new FeatureList<>();
		try {
			// First features
			for( int i=0; i<features.size(); i++ ) {
				NamedBean<Feature<T>> ni = features.get(i);
				
				Feature<T> featureAdd = ni.getValue().duplicateBean();
				featureAdd.setCustomName( ni.getName() );					
				out.add(featureAdd);
			}
		} catch (BeanDuplicateException e) {
			throw new OperationFailedException(e);
		}
		return out;
	}
}
