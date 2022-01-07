/*-
 * #%L
 * anchor-plugin-image
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

package org.anchoranalysis.plugin.image.provider;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.bean.exception.BeanMisconfiguredException;
import org.anchoranalysis.bean.xml.RegisterBeanFactories;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.provider.FeatureProvider;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.image.bean.ImageBean;
import org.anchoranalysis.image.bean.provider.MaskProvider;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.core.mask.Mask;
import org.anchoranalysis.image.io.ImageInitializationFactory;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectCollectionFactory;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.test.LoggerFixture;
import org.anchoranalysis.test.image.InputOutputContextFixture;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProviderFixture {

    public static ObjectCollectionProvider providerFor(ObjectMask obj) {
        return providerFor(ObjectCollectionFactory.of(obj));
    }

    public static ObjectCollectionProvider providerFor(ObjectCollection objects) {
        ObjectCollectionProvider provider = mock(ObjectCollectionProvider.class);
        try {
            when(provider.get()).thenReturn(objects);
        } catch (ProvisionFailedException e) {
        }
        when(provider.duplicateBean()).thenReturn(provider);
        return provider;
    }

    @SuppressWarnings("unchecked")
    public static <T extends FeatureInput> FeatureProvider<T> providerFor(Feature<T> feature) {

        FeatureProvider<T> provider = mock(FeatureProvider.class);
        try {
            when(provider.get()).thenReturn(feature);
        } catch (ProvisionFailedException e) {
        }
        when(provider.duplicateBean()).thenReturn(provider);
        return provider;
    }

    public static MaskProvider providerFor(Mask mask) {
        MaskProvider provider = mock(MaskProvider.class);
        try {
            when(provider.get()).thenReturn(mask);
        } catch (ProvisionFailedException e) {
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

        Logger logger = LoggerFixture.suppressedLogger();

        T provider = funcCreate.create(logger);
        ProviderFixture.initProvider(provider, logger);
        return provider;
    }

    public static <T extends ImageBean<T>> void initProvider(ImageBean<T> provider, Logger logger)
            throws CreateException {
        try {
            provider.checkMisconfigured(RegisterBeanFactories.getDefaultInstances());

            provider.initialize(
                    ImageInitializationFactory.create(InputOutputContextFixture.withLogger(logger)),
                    logger);

        } catch (BeanMisconfiguredException | InitializeException e) {
            throw new CreateException(e);
        }
    }
}
