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

package org.anchoranalysis.plugin.image.task.bean.feature.source;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.calculate.NamedFeatureCalculateException;
import org.anchoranalysis.feature.calculate.bound.FeatureCalculatorMulti;
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.feature.results.ResultsVector;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.core.stack.DisplayStack;
import org.anchoranalysis.image.feature.input.FeatureInputStack;
import org.anchoranalysis.image.io.stack.input.ProvidesStackInput;
import org.anchoranalysis.plugin.image.bean.thumbnail.stack.ScaleToSize;
import org.anchoranalysis.plugin.image.bean.thumbnail.stack.ThumbnailFromStack;
import org.anchoranalysis.plugin.image.task.feature.FeatureCalculationContext;
import org.anchoranalysis.plugin.image.task.feature.ResultsVectorWithThumbnail;
import org.anchoranalysis.plugin.image.task.feature.calculator.FeatureCalculatorFromProvider;

/**
 * Calculates features from a single image.
 *
 * <p>Each image produces a single row of features.
 */
public class FromImage extends SingleRowPerInput<ProvidesStackInput, FeatureInputStack> {

    // START BEAN PROPERTIES
    /**
     * Optionally defines an energy-stack for feature calculation (if not set, the energy-stack is
     * considered to be the input stacks).
     */
    @BeanField @OptionalBean @Getter @Setter private StackProvider stackEnergy;

    /** Method to generate a thumbnail for images. */
    @BeanField @Getter @Setter private ThumbnailFromStack thumbnail = new ScaleToSize();

    // END BEAN PROPERTIES

    /** Creates a new {@link FromImage} instance. */
    public FromImage() {
        super("image");
    }

    @Override
    public InputTypesExpected inputTypesExpected() {
        return new InputTypesExpected(ProvidesStackInput.class);
    }

    @Override
    public boolean includeGroupInExperiment(boolean groupGeneratorDefined) {
        return groupGeneratorDefined;
    }

    @Override
    protected ResultsVectorWithThumbnail calculateResultsForInput(
            ProvidesStackInput input,
            FeatureCalculationContext<FeatureList<FeatureInputStack>> context)
            throws NamedFeatureCalculateException {

        FeatureCalculatorFromProvider<FeatureInputStack> calculator =
                createCalculator(input, context);

        thumbnail.start();

        return new ResultsVectorWithThumbnail(
                () -> calculateResults(calculator, context),
                () -> extractThumbnail(calculator.getEnergyStack(), context.isThumbnailsEnabled()));
    }

    @Override
    protected Optional<String[]> additionalLabelsFor(ProvidesStackInput input) {
        return Optional.empty();
    }

    /**
     * Calculates the results vector and thumbnail for a given input.
     *
     * @param calculator the {@link FeatureCalculatorFromProvider} to use for calculation
     * @param context the {@link FeatureCalculationContext} for calculation
     * @return a {@link ResultsVector} containing the calculated features
     * @throws OperationFailedException if the operation fails
     */
    private ResultsVector calculateResults(
            FeatureCalculatorFromProvider<FeatureInputStack> factory,
            FeatureCalculationContext<FeatureList<FeatureInputStack>> context)
            throws OperationFailedException {
        try {
            return context.getExecutionTimeRecorder()
                    .recordExecutionTime(
                            "Calculating features",
                            () -> {
                                try {
                                    // The energy-stack will be added later, so we do not need to
                                    // initialize it
                                    // in the FeatureInputStack
                                    FeatureCalculatorMulti<FeatureInputStack> calculator =
                                            factory.calculatorForAll(context.getFeatureSource());
                                    return calculator.calculate(
                                            new FeatureInputStack(),
                                            context.getLogger().errorReporter(),
                                            context.isSuppressErrors());
                                } catch (InitializeException e) {
                                    throw new NamedFeatureCalculateException(e);
                                }
                            });
        } catch (NamedFeatureCalculateException e) {
            throw new OperationFailedException(e);
        }
    }

    /**
     * Extracts a thumbnail from the energy stack.
     *
     * @param energyStack the {@link EnergyStack} to extract the thumbnail from
     * @param thumbnails whether thumbnails are enabled
     * @return an {@link Optional} containing the {@link DisplayStack} thumbnail if enabled, empty
     *     otherwise
     * @throws OperationFailedException if the thumbnail extraction fails
     */
    private Optional<DisplayStack> extractThumbnail(EnergyStack energyStack, boolean thumbnails)
            throws OperationFailedException {
        if (thumbnails) {
            try {
                return Optional.of(
                        thumbnail.thumbnailFor(energyStack.withoutParameters().asStack()));
            } catch (CreateException e) {
                throw new OperationFailedException(e);
            }
        } else {
            return Optional.empty();
        }
    }

    /**
     * Creates a {@link FeatureCalculatorFromProvider} for the given input.
     *
     * @param input the {@link ProvidesStackInput} to create the calculator for
     * @param context the {@link FeatureCalculationContext} for calculation
     * @return a new {@link FeatureCalculatorFromProvider} instance
     * @throws NamedFeatureCalculateException if the calculator creation fails
     */
    private FeatureCalculatorFromProvider<FeatureInputStack> createCalculator(
            ProvidesStackInput input,
            FeatureCalculationContext<FeatureList<FeatureInputStack>> context)
            throws NamedFeatureCalculateException {
        try {
            return context.getExecutionTimeRecorder()
                    .recordExecutionTime(
                            "Loading images",
                            () ->
                                    new FeatureCalculatorFromProvider<>(
                                            input,
                                            Optional.ofNullable(stackEnergy),
                                            context.getContext()));
        } catch (OperationFailedException e) {
            throw new NamedFeatureCalculateException(e);
        }
    }
}
