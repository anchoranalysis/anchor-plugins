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
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.InitException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.shared.FeaturesInitialization;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitialization;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ExtractFromProvider {

    public static EnergyStack extractStack(
            StackProvider stackEnergy, ImageInitialization initParams, Logger logger)
            throws OperationFailedException {

        try {
            // Extract the energy stack
            StackProvider providerDuplicated = stackEnergy.duplicateBean();
            providerDuplicated.initRecursive(initParams, logger);

            return new EnergyStack(providerDuplicated.create());
        } catch (InitException | CreateException e) {
            throw new OperationFailedException(e);
        }
    }

    /**
     * Creates and initializes a single-feature that is provided via a {@link FeatureListProvider}
     */
    public static <T extends FeatureInput> Feature<T> extractFeature(
            FeatureListProvider<T> featureListProvider,
            String featureProviderName,
            FeaturesInitialization initParams,
            Logger logger)
            throws FeatureCalculationException {

        try {
            featureListProvider.initRecursive(initParams, logger);

            FeatureList<T> features = featureListProvider.create();
            if (features.size() != 1) {
                throw new FeatureCalculationException(
                        String.format(
                                "%s must return exactly one feature from its list. It currently returns %d",
                                featureProviderName, features.size()));
            }
            return features.get(0);
        } catch (CreateException | InitException e) {
            throw new FeatureCalculationException(e);
        }
    }
}
