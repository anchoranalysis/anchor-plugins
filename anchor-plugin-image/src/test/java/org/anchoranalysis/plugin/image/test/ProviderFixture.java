package org.anchoranalysis.plugin.image.test;

/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan
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
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.init.ImageInitParams;
import org.anchoranalysis.image.objectmask.ObjectMask;
import org.anchoranalysis.image.objectmask.ObjectCollection;
import org.anchoranalysis.image.objectmask.ObjectCollectionFactory;
import org.anchoranalysis.test.LoggingFixture;

public class ProviderFixture {

	private ProviderFixture() {}
	
	public static ObjMaskProvider providerFor(ObjectMask obj) {
		return providerFor(
			ObjectCollectionFactory.from(obj)	
		);
	}
	
	public static ObjMaskProvider providerFor(ObjectCollection objs) {
		ObjMaskProvider provider = mock(ObjMaskProvider.class);
		try {
			when(provider.create()).thenReturn(objs);
		} catch (CreateException e) {
		}
		when(provider.duplicateBean()).thenReturn(provider);
		return provider;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends FeatureInput> FeatureProvider<T> providerFor(Feature<T> feature) {
		
		FeatureProvider<T> provider = mock(FeatureProvider.class);
		try {
			when(provider.create()).thenReturn(feature);
		} catch (CreateException e) {
		}
		when(provider.duplicateBean()).thenReturn(provider);
		return provider;
	}
	
	public static BinaryChnlProvider providerFor(BinaryChnl chnl) {
		BinaryChnlProvider provider = mock(BinaryChnlProvider.class);
		try {
			when(provider.create()).thenReturn(chnl);
		} catch (CreateException e) {
		}
		when(provider.duplicateBean()).thenReturn(provider);
		return provider;
	}
		
	@FunctionalInterface
	public interface CreateProvider<T> {
		
		public T create( LogErrorReporter logger ) throws CreateException;
	}
	
	public static <T extends ImageBean<T>> T createInitMergePair( CreateProvider<T> funcCreate ) throws CreateException {
		
		LogErrorReporter logger = LoggingFixture.suppressedLogErrorReporter();
		
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
