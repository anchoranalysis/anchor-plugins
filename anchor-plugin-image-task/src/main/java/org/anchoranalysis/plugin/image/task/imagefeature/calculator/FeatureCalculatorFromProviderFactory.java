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
/* (C)2020 */
package org.anchoranalysis.plugin.image.task.imagefeature.calculator;

import java.util.Optional;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.FeatureInitParams;
import org.anchoranalysis.feature.input.FeatureInputNRG;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.feature.session.FeatureSession;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorMulti;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorMultiChangeInput;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingleChangeInput;
import org.anchoranalysis.feature.shared.SharedFeatureMulti;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.io.input.ProvidesStackInput;
import org.anchoranalysis.image.io.input.StackInputInitParamsCreator;
import org.anchoranalysis.io.output.bound.BoundIOContext;

/**
 * Calculates feature or feature values, adding a nrgStack (optionally) from either provider or the
 * input-stack
 *
 * @author Owen Feehan
 * @param <T> feature-input-type
 */
public class FeatureCalculatorFromProviderFactory<T extends FeatureInputNRG> {

    private final ImageInitParams initParams;
    private final NRGStackWithParams nrgStack;
    private final Logger logger;

    public FeatureCalculatorFromProviderFactory(
            ProvidesStackInput stackInput,
            Optional<StackProvider> nrgStackProvider,
            BoundIOContext context)
            throws OperationFailedException {
        super();
        this.initParams = StackInputInitParamsCreator.createInitParams(stackInput, context);
        this.nrgStack =
                nrgStackFromProviderOrInput(stackInput, nrgStackProvider, context.getLogger());
        this.logger = context.getLogger();
    }

    /**
     * Calculates a single-feature that comes from a provider (but can reference the other features
     * from the store)
     *
     * @throws FeatureCalcException
     */
    public FeatureCalculatorSingle<T> calculatorSingleFromProvider(
            FeatureListProvider<T> provider, String providerName) throws FeatureCalcException {

        Feature<T> feature =
                ExtractFromProvider.extractFeature(
                        provider, providerName, initParams.getFeature(), logger);

        return createSingleCalculator(feature, initParams.getFeature().getSharedFeatureSet());
    }

    /** Calculates all image-features in the feature-store */
    public FeatureCalculatorMulti<T> calculatorForAll(FeatureList<T> features)
            throws FeatureCalcException {
        return createMultiCalculator(features, initParams.getFeature().getSharedFeatureSet());
    }

    /**
     * Calculates a NRG-stack from a provider if it's available, or otherwise uses the input as the
     * nerg
     */
    private NRGStackWithParams nrgStackFromProviderOrInput(
            ProvidesStackInput stackInput, Optional<StackProvider> nrgStackProvider, Logger logger)
            throws OperationFailedException {
        if (nrgStackProvider.isPresent()) {
            return ExtractFromProvider.extractStack(nrgStackProvider.get(), initParams, logger);
        } else {
            return new NRGStackWithParams(stackInput.extractSingleStack());
        }
    }

    private FeatureCalculatorMulti<T> createMultiCalculator(
            FeatureList<T> features, SharedFeatureMulti sharedFeatures)
            throws FeatureCalcException {
        return new FeatureCalculatorMultiChangeInput<>(
                FeatureSession.with(features, new FeatureInitParams(), sharedFeatures, logger),
                input -> input.setNrgStack(nrgStack));
    }

    private FeatureCalculatorSingle<T> createSingleCalculator(
            Feature<T> feature, SharedFeatureMulti sharedFeatures) throws FeatureCalcException {
        return new FeatureCalculatorSingleChangeInput<>(
                FeatureSession.with(feature, new FeatureInitParams(), sharedFeatures, logger),
                input -> input.setNrgStack(nrgStack));
    }
}
