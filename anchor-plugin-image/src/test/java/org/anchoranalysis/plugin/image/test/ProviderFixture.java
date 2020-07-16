/* (C)2020 */
package org.anchoranalysis.plugin.image.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.bean.xml.RegisterBeanFactories;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.provider.FeatureProvider;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.image.bean.ImageBean;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.io.input.ImageInitParamsFactory;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.test.LoggingFixture;
import org.anchoranalysis.test.image.BoundIOContextFixture;

public class ProviderFixture {

    private ProviderFixture() {}

    public static ObjectCollectionProvider providerFor(ObjectMask obj) {
        return providerFor(ObjectCollectionFactory.from(obj));
    }

    public static ObjectCollectionProvider providerFor(ObjectCollection objects) {
        ObjectCollectionProvider provider = mock(ObjectCollectionProvider.class);
        try {
            when(provider.create()).thenReturn(objects);
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

    public static BinaryChnlProvider providerFor(Mask chnl) {
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

        public T create(Logger logger) throws CreateException;
    }

    public static <T extends ImageBean<T>> T createInitMergePair(CreateProvider<T> funcCreate)
            throws CreateException {

        Logger logger = LoggingFixture.suppressedLogErrorReporter();

        T provider = funcCreate.create(logger);
        ProviderFixture.initProvider(provider, logger);
        return provider;
    }

    public static <T extends ImageBean<T>> void initProvider(ImageBean<T> provider, Logger logger)
            throws CreateException {
        try {
            provider.checkMisconfigured(RegisterBeanFactories.getDefaultInstances());

            provider.init(
                    ImageInitParamsFactory.create(BoundIOContextFixture.withLogger(logger)),
                    logger);

        } catch (BeanMisconfiguredException | InitException e) {
            throw new CreateException(e);
        }
    }
}
