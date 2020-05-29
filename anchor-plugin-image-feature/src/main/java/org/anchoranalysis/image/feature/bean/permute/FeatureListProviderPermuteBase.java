package org.anchoranalysis.image.feature.bean.permute;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.SkipInit;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.input.FeatureInput;

public abstract class FeatureListProviderPermuteBase<T extends FeatureInput> extends FeatureListProvider<T> {

	// START BEAN PROPERTIES
	@BeanField @SkipInit
	private Feature<T> feature;
	// END BEAN PROPERTIES
	
	@Override
	public FeatureList<T> create() throws CreateException {
		return createPermutedFeaturesFor(feature);
	}
	
	protected abstract FeatureList<T> createPermutedFeaturesFor( Feature<T> feature ) throws CreateException;
	
	public Feature<T> getFeature() {
		return feature;
	}

	public void setFeature(Feature<T> feature) {
		this.feature = feature;
	}
}
