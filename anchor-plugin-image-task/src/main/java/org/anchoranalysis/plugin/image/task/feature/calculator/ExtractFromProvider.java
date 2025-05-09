/*-
 * #%L
 * anchor-plugin-image-task
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

package org.anchoranalysis.plugin.image.task.feature.calculator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.feature.initialization.FeatureRelatedInitialization;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitialization;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;

/** Utility class for extracting stacks and features from providers. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ExtractFromProvider {

    /**
     * Extracts an {@link EnergyStack} from a {@link StackProvider}.
     *
     * @param stackEnergy the stack provider to extract from
     * @param initialization the image initialization context
     * @param logger the logger for recording messages
     * @return the extracted {@link EnergyStack}
     * @throws OperationFailedException if the extraction fails
     */
    public static EnergyStack extractStack(
            StackProvider stackEnergy, ImageInitialization initialization, Logger logger)
            throws OperationFailedException {

        try {
            // Extract the energy stack
            StackProvider providerDuplicated = stackEnergy.duplicateBean();
            providerDuplicated.initializeRecursive(initialization, logger);

            return new EnergyStack(providerDuplicated.get());
        } catch (InitializeException | ProvisionFailedException e) {
            throw new OperationFailedException(e);
        }
    }

    /**
     * Creates and initializes a single-feature that is provided via a {@link FeatureListProvider}.
     *
     * @param <T> the type of feature input
     * @param featureListProvider the provider of the feature list
     * @param featureProviderName the name of the feature provider
     * @param initialization the feature-related initialization context
     * @param logger the logger for recording messages
     * @return the extracted {@link Feature}
     * @throws FeatureCalculationException if the extraction fails or if more than one feature is
     *     provided
     */
    public static <T extends FeatureInput> Feature<T> extractFeature(
            FeatureListProvider<T> featureListProvider,
            String featureProviderName,
            FeatureRelatedInitialization initialization,
            Logger logger)
            throws FeatureCalculationException {

        try {
            featureListProvider.initializeRecursive(initialization, logger);

            FeatureList<T> features = featureListProvider.get();
            if (features.size() != 1) {
                throw new FeatureCalculationException(
                        String.format(
                                "%s must return exactly one feature from its list. It currently returns %d",
                                featureProviderName, features.size()));
            }
            return features.get(0);
        } catch (InitializeException | ProvisionFailedException e) {
            throw new FeatureCalculationException(e);
        }
    }
}
