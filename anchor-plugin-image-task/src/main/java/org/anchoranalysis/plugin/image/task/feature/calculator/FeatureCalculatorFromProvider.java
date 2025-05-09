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

import java.util.Optional;
import lombok.Getter;
import org.anchoranalysis.core.cache.CachedSupplier;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.identifier.provider.NamedProviderGetException;
import org.anchoranalysis.core.identifier.provider.store.NamedProviderStore;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.time.ExecutionTimeRecorder;
import org.anchoranalysis.experiment.io.InitializationContext;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.bound.FeatureCalculatorMulti;
import org.anchoranalysis.feature.calculate.bound.FeatureCalculatorMultiChangeInput;
import org.anchoranalysis.feature.calculate.bound.FeatureCalculatorSingle;
import org.anchoranalysis.feature.calculate.bound.FeatureCalculatorSingleChangeInput;
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.feature.initialization.FeatureInitialization;
import org.anchoranalysis.feature.input.FeatureInputEnergy;
import org.anchoranalysis.feature.session.FeatureSession;
import org.anchoranalysis.feature.shared.SharedFeatures;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitialization;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.core.dimensions.IncorrectImageSizeException;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.io.stack.input.ProvidesStackInput;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.plugin.image.task.stack.InitializationFactory;

/**
 * Calculates feature or feature values.
 *
 * <p>Optionally, an energy-stack is added from either provider or the input-stack.
 *
 * @param <T> feature-input-type extending {@link FeatureInputEnergy}
 */
public class FeatureCalculatorFromProvider<T extends FeatureInputEnergy> {

    /** The initialization for image-related operations. */
    private final ImageInitialization initialization;

    /** The energy stack used for feature calculations. */
    @Getter private final EnergyStack energyStack;

    /** The logger for recording messages. */
    private final Logger logger;

    /**
     * Creates a new {@link FeatureCalculatorFromProvider}.
     *
     * @param stackInput provides the input stack
     * @param stackEnergy optional provider for the energy stack
     * @param context the input-output context
     * @throws OperationFailedException if the initialization or energy stack creation fails
     */
    public FeatureCalculatorFromProvider(
            ProvidesStackInput stackInput,
            Optional<StackProvider> stackEnergy,
            InputOutputContext context)
            throws OperationFailedException {
        this.initialization =
                context.getExecutionTimeRecorder()
                        .recordExecutionTime(
                                "Creating image-initialization",
                                () ->
                                        InitializationFactory.createWithStacks(
                                                stackInput, new InitializationContext(context)));

        // Caches the loading of the stack for the feature.
        CachedSupplier<Stack, OperationFailedException> loadImage =
                CachedSupplier.cacheChecked(
                        () ->
                                allStacksAsOne(
                                        initialization.stacks(),
                                        context.getExecutionTimeRecorder()));

        this.energyStack =
                context.getExecutionTimeRecorder()
                        .recordExecutionTime(
                                "Loading feature calculation stack",
                                () ->
                                        energyStackFromProviderOrElse(
                                                stackEnergy, loadImage, context.getLogger()));
        this.logger = context.getLogger();
    }

    /**
     * Calculates a single-feature that comes from a provider (but can reference the other features
     * from the store).
     *
     * @param provider the feature list provider
     * @param providerName the name of the provider
     * @return a {@link FeatureCalculatorSingle} for the specified feature
     * @throws OperationFailedException if the feature calculation fails
     */
    public FeatureCalculatorSingle<T> calculatorSingleFromProvider(
            FeatureListProvider<T> provider, String providerName) throws OperationFailedException {

        try {
            Feature<T> feature =
                    ExtractFromProvider.extractFeature(
                            provider,
                            providerName,
                            initialization.featuresInitialization(),
                            logger);

            return createSingleCalculator(
                    feature, initialization.featuresInitialization().getSharedFeatures());
        } catch (InitializeException | FeatureCalculationException e) {
            throw new OperationFailedException(e);
        }
    }

    /**
     * Calculates all image-features in the feature-store.
     *
     * @param features the list of features to calculate
     * @return a {@link FeatureCalculatorMulti} for the specified features
     * @throws InitializeException if the calculator initialization fails
     */
    public FeatureCalculatorMulti<T> calculatorForAll(FeatureList<T> features)
            throws InitializeException {
        return createMultiCalculator(
                features, initialization.featuresInitialization().getSharedFeatures());
    }

    /**
     * Calculates an energy-stack from a provider if it's available, or otherwise uses a fallback.
     *
     * @param stackEnergy optional provider for the energy stack
     * @param fallback fallback supplier for the stack
     * @param logger the logger for recording messages
     * @return the {@link EnergyStack} created from the provider or fallback
     * @throws OperationFailedException if the energy stack creation fails
     */
    private EnergyStack energyStackFromProviderOrElse(
            Optional<StackProvider> stackEnergy,
            CachedSupplier<Stack, OperationFailedException> fallback,
            Logger logger)
            throws OperationFailedException {
        if (stackEnergy.isPresent()) {
            return ExtractFromProvider.extractStack(stackEnergy.get(), initialization, logger);
        } else {
            return new EnergyStack(fallback.get());
        }
    }

    /**
     * Creates a multi-feature calculator.
     *
     * @param features the list of features to calculate
     * @param sharedFeatures shared features for the calculation
     * @return a {@link FeatureCalculatorMulti} for the specified features
     * @throws InitializeException if the calculator initialization fails
     */
    private FeatureCalculatorMulti<T> createMultiCalculator(
            FeatureList<T> features, SharedFeatures sharedFeatures) throws InitializeException {
        return new FeatureCalculatorMultiChangeInput<>(
                FeatureSession.with(features, new FeatureInitialization(), sharedFeatures, logger),
                input -> input.setEnergyStack(energyStack));
    }

    /**
     * Creates a single-feature calculator.
     *
     * @param feature the feature to calculate
     * @param sharedFeatures shared features for the calculation
     * @return a {@link FeatureCalculatorSingle} for the specified feature
     * @throws InitializeException if the calculator initialization fails
     */
    private FeatureCalculatorSingle<T> createSingleCalculator(
            Feature<T> feature, SharedFeatures sharedFeatures) throws InitializeException {
        return new FeatureCalculatorSingleChangeInput<>(
                FeatureSession.with(feature, new FeatureInitialization(), sharedFeatures, logger),
                input -> input.setEnergyStack(energyStack));
    }

    /**
     * Combines all stacks in the store into one stack.
     *
     * <p>There is no guarantee about the ordering of the stacks, if there are multiple stacks in
     * the store.
     *
     * <p>All stacks must have the same dimensions.
     *
     * @param store a named-store of stacks
     * @param executionTimeRecorder records execution time of operations
     * @return a {@link Stack} with channels from all stacks in the store
     * @throws OperationFailedException if the stacks have different dimensions, or if anything else
     *     goes wrong
     */
    private static Stack allStacksAsOne(
            NamedProviderStore<Stack> store, ExecutionTimeRecorder executionTimeRecorder)
            throws OperationFailedException {
        try {
            Stack out = new Stack();

            for (String key : store.keys()) {
                Stack channel =
                        executionTimeRecorder.recordExecutionTime(
                                "Loading channel", () -> store.getOptional(key).get()); // NOSONAR
                out.addChannelsFrom(channel);
            }

            return out;

        } catch (NamedProviderGetException | IncorrectImageSizeException e) {
            throw new OperationFailedException(e);
        }
    }
}
