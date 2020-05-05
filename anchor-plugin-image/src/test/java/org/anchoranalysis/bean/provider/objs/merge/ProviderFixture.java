package org.anchoranalysis.bean.provider.objs.merge;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.bean.xml.RegisterBeanFactories;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.provider.FeatureProvider;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.image.bean.ImageBean;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.init.ImageInitParams;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.test.LoggingFixture;

class ProviderFixture {

	private ProviderFixture() {}
	
	public static ObjMaskProvider providerFor(ObjMaskCollection objs) throws CreateException {
		ObjMaskProvider provider = mock(ObjMaskProvider.class);
		when(provider.create()).thenReturn(objs);
		when(provider.duplicateBean()).thenReturn(provider);
		return provider;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends FeatureInput> FeatureProvider<T> providerFor(Feature<T> feature) throws CreateException {
		
		FeatureProvider<T> provider = mock(FeatureProvider.class);
		when(provider.create()).thenReturn(feature);
		when(provider.duplicateBean()).thenReturn(provider);
		return provider;
	}
		
	@FunctionalInterface
	public interface CreateProvider<T> {
		
		public T create( LogErrorReporter logger ) throws CreateException;
	}
	
	public static <T extends ImageBean<T>> T createInitMergePair( CreateProvider<T> funcCreate ) throws CreateException {
		
		LogErrorReporter logger = LoggingFixture.simpleLogErrorReporter();
		
		T provider = funcCreate.create(logger);
		ProviderFixture.initProvider(provider, logger);
		return provider;
	}
	
	public static <T extends ImageBean<T>> void initProvider( ImageBean<T> provider, LogErrorReporter logger) throws CreateException {
		try {
			provider.checkMisconfigured( RegisterBeanFactories.getDefaultInstances() );
			
			provider.init(
				mock(ImageInitParams.class),
				logger
			);
			
		} catch (BeanMisconfiguredException | InitException e) {
			throw new CreateException(e);
		}
	}
}