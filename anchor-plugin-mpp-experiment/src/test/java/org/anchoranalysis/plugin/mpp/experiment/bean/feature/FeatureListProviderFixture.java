package org.anchoranalysis.plugin.mpp.experiment.bean.feature;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.bean.list.FeatureListProviderDefine;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.feature.plugins.FeaturesFromXmlFixture;

/**
 * Loads a feature-list to be used in tests.
 * 
 * <p>By default, this comes from a particular XML file in the test-resources, but methods
 * allow other possibilities</p>
 * 
 * @param <T> feature-input-type returned by the list
 * 
 * @author Owen Feehan
 *
 */
class FeatureListProviderFixture<T extends FeatureInput> {

	private static final String SINGLE_FEATURE_NAME = "someFeature";
	
	private TestLoader loader;
	private String defaultPath;
	
	/** The to use, if set. If empty, the default is used instead. */
	private Optional<List<NamedBean<
		FeatureListProvider<T>
	>>> features = Optional.empty();
	
	/**
	 * Constructor
	 * 
	 * @param loader a loader for finding XML files
	 * @param defaultPathToXml the features in this XML file are used if no other option is set
	 */
	public FeatureListProviderFixture(TestLoader loader, String defaultPathToXml) {
		super();
		this.loader = loader;
		this.defaultPath = defaultPathToXml;
	}
	
	/** Additionally include a shell feature in the "single" features 
	 * @throws CreateException */
	public void useAlternativeXMLList( String alternativePathToXml ) throws CreateException {
		features = Optional.of(
			loadFeatures(loader, alternativePathToXml)
		);
	}
	
	/** 
	 * Uses this feature as the only item in the list.
	 * 
	 * <p>Any XML-based features are ignored.</p>
	 * <p>It does not initialize the feature.</p>
	 * */
	public void useSingleFeature( Feature<T> feature ) {
		
		NamedBean<FeatureListProvider<T>> nb = new NamedBean<>(
			SINGLE_FEATURE_NAME,
			new FeatureListProviderDefine<>(feature)
		);
		
		this.features = Optional.of(
			Arrays.asList(nb)
		);
	}
	
	/** Creates the features as a list of providers in named-beans */
	public List<NamedBean<FeatureListProvider<T>>> asListNamedBeansProvider() throws CreateException {
		if (features.isPresent()) {
			return features.get();
		} else {
			return loadFeatures(loader, defaultPath);
		}
	}
	
	/** creates a feature-list associated with obj-mask
	 *  
	 * @throws CreateException 
	 * */
	private static <S extends FeatureInput> List<NamedBean<FeatureListProvider<S>>> loadFeatures( TestLoader loader, String pathFeatureList ) throws CreateException {
		return FeaturesFromXmlFixture.createNamedFeatureProviders(pathFeatureList, loader);
	}
	
}
