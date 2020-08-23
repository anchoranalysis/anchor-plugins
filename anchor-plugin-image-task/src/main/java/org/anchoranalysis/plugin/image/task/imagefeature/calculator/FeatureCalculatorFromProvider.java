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

package org.anchoranalysis.plugin.image.task.imagefeature.calculator;

import java.util.Optional;
import lombok.Getter;
import org.anchoranalysis.core.cache.CachedSupplier;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.core.name.store.NamedProviderStore;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.FeatureInitParams;
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.feature.input.FeatureInputEnergy;
import org.anchoranalysis.feature.session.FeatureSession;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorMulti;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorMultiChangeInput;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingleChangeInput;
import org.anchoranalysis.feature.shared.SharedFeatureMulti;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.extent.IncorrectImageSizeException;
import org.anchoranalysis.image.io.input.ProvidesStackInput;
import org.anchoranalysis.image.io.input.StackInputInitParamsCreator;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.output.bound.BoundIOContext;

/**
 * Calculates feature or feature values, adding a energyStack (optionally) from either provider or the
 * input-stack
 *
 * @author Owen Feehan
 * @param <T> feature-input-type
 */
public class FeatureCalculatorFromProvider<T extends FeatureInputEnergy> {

    private final ImageInitParams initParams;

    @Getter private final EnergyStack energyStack;

    private final Logger logger;

    public FeatureCalculatorFromProvider(
            ProvidesStackInput stackInput,
            Optional<StackProvider> stackEnergy,
            BoundIOContext context)
            throws OperationFailedException {
        super();
        this.initParams = StackInputInitParamsCreator.createInitParams(stackInput, context);
        this.energyStack =
                energyStackFromProviderOrElse(
                        stackEnergy,
                        CachedSupplier.cache(() -> allStacksAsOne(initParams.stacks())),
                        context.getLogger());
        this.logger = context.getLogger();
    }

    /**
     * Calculates a single-feature that comes from a provider (but can reference the other features
     * from the store)
     *
     * @throws FeatureCalculationException
     */
    public FeatureCalculatorSingle<T> calculatorSingleFromProvider(
            FeatureListProvider<T> provider, String providerName) throws OperationFailedException {

        try {
            Feature<T> feature =
                    ExtractFromProvider.extractFeature(
                            provider, providerName, initParams.features(), logger);

            return createSingleCalculator(feature, initParams.features().getSharedFeatureSet());
        } catch (InitException | FeatureCalculationException e) {
            throw new OperationFailedException(e);
        }
    }

    /** Calculates all image-features in the feature-store */
    public FeatureCalculatorMulti<T> calculatorForAll(FeatureList<T> features)
            throws InitException {
        return createMultiCalculator(features, initParams.features().getSharedFeatureSet());
    }

    /** Calculates a energy-stack from a provider if it's available, or otherwise uses a fallback */
    private EnergyStack energyStackFromProviderOrElse(
            Optional<StackProvider> stackEnergy,
            CachedSupplier<Stack, OperationFailedException> fallback,
            Logger logger)
            throws OperationFailedException {
        if (stackEnergy.isPresent()) {
            return ExtractFromProvider.extractStack(stackEnergy.get(), initParams, logger);
        } else {
            return new EnergyStack(fallback.get());
        }
    }

    private FeatureCalculatorMulti<T> createMultiCalculator(
            FeatureList<T> features, SharedFeatureMulti sharedFeatures) throws InitException {
        return new FeatureCalculatorMultiChangeInput<>(
                FeatureSession.with(features, new FeatureInitParams(), sharedFeatures, logger),
                input -> input.setEnergyStack(energyStack));
    }

    private FeatureCalculatorSingle<T> createSingleCalculator(
            Feature<T> feature, SharedFeatureMulti sharedFeatures) throws InitException {
        return new FeatureCalculatorSingleChangeInput<>(
                FeatureSession.with(feature, new FeatureInitParams(), sharedFeatures, logger),
                input -> input.setEnergyStack(energyStack));
    }

    /**
     * Combines all stacks in the store into one stack
     *
     * <p>There is no guarantee about the ordering of the stacks, if there are multiple stacks in
     * the store.
     *
     * <p>All stacks must have the same dimensions.
     *
     * @param store a named-store of stacks
     * @return a stack with channels from all stacks in the store
     * @throws OperationFailedException if the stacks have different dimensions, or if anything else
     *     goes wrong
     */
    private static Stack allStacksAsOne(NamedProviderStore<Stack> store)
            throws OperationFailedException {
        try {
            Stack out = new Stack();

            for (String key : store.keys()) {
                out.addChannelsFrom(store.getOptional(key).get()); // NOSONAR
            }

            return out;

        } catch (NamedProviderGetException | IncorrectImageSizeException e) {
            throw new OperationFailedException(e);
        }
    }
}
