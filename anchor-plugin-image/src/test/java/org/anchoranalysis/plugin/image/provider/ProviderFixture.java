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
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.exception.friendly.AnchorImpossibleSituationException;
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
import org.anchoranalysis.test.image.io.BeanInstanceMapFixture;

/** Utility class for creating mock providers for testing purposes. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProviderFixture {

    /**
     * Creates a mock {@link ObjectCollectionProvider} for a single {@link ObjectMask}.
     *
     * @param obj the {@link ObjectMask} to provide
     * @return a mock {@link ObjectCollectionProvider}
     */
    public static ObjectCollectionProvider providerFor(ObjectMask obj) {
        return providerFor(ObjectCollectionFactory.of(obj));
    }

    /**
     * Creates a mock {@link ObjectCollectionProvider} for an {@link ObjectCollection}.
     *
     * @param objects the {@link ObjectCollection} to provide
     * @return a mock {@link ObjectCollectionProvider}
     */
    public static ObjectCollectionProvider providerFor(ObjectCollection objects) {
        ObjectCollectionProvider provider = mock(ObjectCollectionProvider.class);
        try {
            when(provider.get()).thenReturn(objects);
        } catch (ProvisionFailedException e) {
            throw new AnchorImpossibleSituationException();
        }
        when(provider.duplicateBean()).thenReturn(provider);
        return provider;
    }

    /**
     * Creates a mock {@link FeatureProvider} for a {@link Feature}.
     *
     * @param <T> the type of {@link FeatureInput}
     * @param feature the {@link Feature} to provide
     * @return a mock {@link FeatureProvider}
     */
    @SuppressWarnings("unchecked")
    public static <T extends FeatureInput> FeatureProvider<T> providerFor(Feature<T> feature) {
        FeatureProvider<T> provider = mock(FeatureProvider.class);
        try {
            when(provider.get()).thenReturn(feature);
        } catch (ProvisionFailedException e) {
            throw new AnchorImpossibleSituationException();
        }
        when(provider.duplicateBean()).thenReturn(provider);
        return provider;
    }

    /**
     * Creates a mock {@link MaskProvider} for a {@link Mask}.
     *
     * @param mask the {@link Mask} to provide
     * @return a mock {@link MaskProvider}
     */
    public static MaskProvider providerFor(Mask mask) {
        MaskProvider provider = mock(MaskProvider.class);
        try {
            when(provider.get()).thenReturn(mask);
        } catch (ProvisionFailedException e) {
            throw new AnchorImpossibleSituationException();
        }
        when(provider.duplicateBean()).thenReturn(provider);
        return provider;
    }

    /**
     * Functional interface for creating a provider.
     *
     * @param <T> the type of provider to create
     */
    @FunctionalInterface
    public interface CreateProvider<T> {
        /**
         * Creates a provider.
         *
         * @param logger the {@link Logger} to use
         * @return the created provider
         * @throws CreateException if the provider cannot be created
         */
        public T create(Logger logger) throws CreateException;
    }

    /**
     * Creates and initializes a provider.
     *
     * @param <T> the type of {@link ImageBean} to create
     * @param funcCreate the function to create the provider
     * @return the created and initialized provider
     * @throws CreateException if the provider cannot be created or initialized
     */
    public static <T extends ImageBean<T>> T createInitMergePair(CreateProvider<T> funcCreate)
            throws CreateException {
        Logger logger = LoggerFixture.suppressedLogger();
        T provider = funcCreate.create(logger);
        ProviderFixture.initProvider(provider, logger);
        return provider;
    }

    /**
     * Initializes an {@link ImageBean}.
     *
     * @param <T> the type of {@link ImageBean} to initialize
     * @param provider the provider to initialize
     * @param logger the {@link Logger} to use
     * @throws CreateException if the provider cannot be initialized
     */
    public static <T extends ImageBean<T>> void initProvider(ImageBean<T> provider, Logger logger)
            throws CreateException {
        try {
            BeanInstanceMapFixture.check(provider);
            provider.initialize(
                    ImageInitializationFactory.create(InputOutputContextFixture.withLogger(logger)),
                    logger);
        } catch (InitializeException e) {
            throw new CreateException(e);
        }
    }
}
