/*-
 * #%L
 * anchor-plugin-mpp-experiment
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

package org.anchoranalysis.plugin.mpp.experiment.bean.feature.source;

import lombok.AllArgsConstructor;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.InitException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitialization;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.plugin.image.task.feature.InitializationWithEnergyStack;
import org.anchoranalysis.plugin.image.task.feature.calculator.CalculateFeaturesForObjects;
import org.anchoranalysis.plugin.image.task.feature.calculator.CalculateFeaturesForObjects.LabelsForInput;

@AllArgsConstructor
class CalculateFeaturesFromProvider<T extends FeatureInput> {

    private final CalculateFeaturesForObjects<T> calculator;
    private final InitializationWithEnergyStack initialization;

    public void processProvider(ObjectCollectionProvider provider, LabelsForInput<T> labelsForInput)
            throws OperationFailedException {
        calculator.calculateFeaturesForObjects(
                objectsFromProvider(provider, initialization.getImage(), calculator.getLogger()),
                initialization.getEnergyStack(),
                labelsForInput);
    }

    private static ObjectCollection objectsFromProvider(
            ObjectCollectionProvider provider, ImageInitialization initialization, Logger logger)
            throws OperationFailedException {

        try {
            ObjectCollectionProvider providerDuplicated = provider.duplicateBean();

            // Initialise
            providerDuplicated.initRecursive(initialization, logger);

            return providerDuplicated.get();

        } catch (InitException | ProvisionFailedException e) {
            throw new OperationFailedException(e);
        }
    }
}
