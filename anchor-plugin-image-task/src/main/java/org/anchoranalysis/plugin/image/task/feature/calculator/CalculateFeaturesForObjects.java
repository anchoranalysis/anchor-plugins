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
package org.anchoranalysis.plugin.image.task.feature.calculator;

import java.util.Optional;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.feature.calculate.NamedFeatureCalculateException;
import org.anchoranalysis.feature.calculate.bound.FeatureCalculatorMulti;
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.io.csv.metadata.RowLabels;
import org.anchoranalysis.feature.io.results.LabelledResultsVector;
import org.anchoranalysis.feature.results.ResultsVector;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitialization;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.core.stack.DisplayStack;
import org.anchoranalysis.image.feature.calculator.FeatureTableCalculator;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.plugin.image.feature.bean.object.combine.CombineObjectsForFeatures;
import org.anchoranalysis.plugin.image.feature.object.ListWithThumbnails;
import org.anchoranalysis.plugin.image.task.feature.FeatureCalculationContext;
import org.anchoranalysis.plugin.image.task.feature.InitializationWithEnergyStack;
import org.anchoranalysis.plugin.image.thumbnail.ThumbnailBatch;

public class CalculateFeaturesForObjects<T extends FeatureInput> {

    private final CombineObjectsForFeatures<T> table;
    private final FeatureCalculatorMulti<T> calculator;
    private final InitializationWithEnergyStack initialization;

    /**
     * When true, no exceptions are thrown when an error occurs, but rather a message is written to
     * the log.
     */
    private final boolean suppressErrors;

    /** Labels to describe {@code objects}. */
    private final FeatureCalculationContext<FeatureTableCalculator<T>> context;

    public CalculateFeaturesForObjects(
            CombineObjectsForFeatures<T> table,
            InitializationWithEnergyStack initialization,
            boolean suppressErrors,
            FeatureCalculationContext<FeatureTableCalculator<T>> context)
            throws OperationFailedException {
        this.table = table;
        this.calculator =
                startCalculator(context.getFeatureSource(), initialization, context.getLogger());
        this.suppressErrors = suppressErrors;
        this.initialization = initialization;
        this.context = context;
    }

    public void calculateForObjects(
            ObjectCollectionProvider provider, LabelsForInput labelsForInput)
            throws OperationFailedException {
        ObjectCollection objects =
                context.getExecutionTimeRecorder()
                        .recordExecutionTime(
                                "Objects from provider",
                                () ->
                                        objectsFromProvider(
                                                provider,
                                                initialization.getImage(),
                                                context.getLogger()));
        calculateForObjects(objects, initialization.getEnergyStack(), labelsForInput);
    }

    /**
     * Calculates the feature-results for {@code objects}, and stores the results.
     *
     * @param objects the objects to calculate features for.
     * @param labelsForInput how to assign labels to the input.
     * @param energyStack the energy-stack.
     * @throws OperationFailedException if the operation cannot be completed, but not if a
     *     particular feature calculation fails.
     */
    public void calculateForObjects(
            ObjectCollection objects, EnergyStack energyStack, LabelsForInput labelsForInput)
            throws OperationFailedException {
        try {
            ListWithThumbnails<T, ObjectCollection> inputs =
                    context.getExecutionTimeRecorder()
                            .recordExecutionTime(
                                    "Derive inputs start batch",
                                    () ->
                                            table.deriveInputsStartBatch(
                                                    objects,
                                                    energyStack,
                                                    context.isThumbnailsEnabled(),
                                                    context.getLogger()));

            calculateManyFeaturesInto(inputs, labelsForInput);

        } catch (CreateException | OperationFailedException e) {
            throw new OperationFailedException(e);
        }
    }

    private static ObjectCollection objectsFromProvider(
            ObjectCollectionProvider provider, ImageInitialization initialization, Logger logger)
            throws OperationFailedException {

        try {
            ObjectCollectionProvider providerDuplicated = provider.duplicateBean();

            // Initialise
            providerDuplicated.initializeRecursive(initialization, logger);

            return providerDuplicated.get();

        } catch (InitializeException | ProvisionFailedException e) {
            throw new OperationFailedException(e);
        }
    }

    /**
     * Calculates a bunch of features with an objectID (unique) and a groupID and adds them to the
     * stored-results.
     *
     * <p>The stored-results also have an additional first-column with the ID.
     *
     * @param listInputs a list of parameters. Each parameters creates a new result (e.g. a new row
     *     in a feature-table)
     * @throws OperationFailedException
     */
    private void calculateManyFeaturesInto(
            ListWithThumbnails<T, ObjectCollection> listInputs, LabelsForInput labelsForInput)
            throws OperationFailedException {

        for (int i = 0; i < listInputs.size(); i++) {
            final int index = i;
            T input = listInputs.get(index);
            context.getResults()
                    .add(
                            () ->
                                    calculateLabelledResults(
                                            input, listInputs, labelsForInput, index),
                            () ->
                                    thumbnailForInput(
                                            input,
                                            listInputs.getThumbnailBatch(),
                                            context.isThumbnailsEnabled()));
        }
    }

    private LabelledResultsVector calculateLabelledResults(
            T input,
            ListWithThumbnails<T, ObjectCollection> listInputs,
            LabelsForInput labelsForInput,
            int index)
            throws OperationFailedException {
        try {
            context.getLogger()
                    .messageLogger()
                    .logFormatted(
                            "Calculating input %d of %d: %s",
                            index + 1, listInputs.size(), input.toString());

            ResultsVector results =
                    calculator.calculate(
                            input, context.getLogger().errorReporter(), suppressErrors);
            String objectIdentifier = table.uniqueIdentifierFor(input);
            RowLabels labels =
                    labelsForInput.deriveLabels(
                            objectIdentifier, context.getGroupGeneratorName(), index);
            return new LabelledResultsVector(labels, results);
        } catch (NamedFeatureCalculateException e) {
            throw new OperationFailedException(e);
        }
    }

    private Optional<DisplayStack> thumbnailForInput(
            T input,
            Optional<ThumbnailBatch<ObjectCollection>> thumbnailBatch,
            boolean thumbnailsEnabled)
            throws OperationFailedException {
        if (thumbnailsEnabled && thumbnailBatch.isPresent()) {
            try {
                return Optional.of(
                        thumbnailBatch.get().thumbnailFor(table.objectsForThumbnail(input)));
            } catch (CreateException e) {
                throw new OperationFailedException(e);
            }
        } else {
            return Optional.empty();
        }
    }

    private static <T extends FeatureInput> FeatureCalculatorMulti<T> startCalculator(
            FeatureTableCalculator<T> calculator,
            InitializationWithEnergyStack initialization,
            Logger logger)
            throws OperationFailedException {

        try {
            calculator.start(
                    initialization.getImage(),
                    Optional.of(initialization.getEnergyStack()),
                    logger);
        } catch (InitializeException e) {
            throw new OperationFailedException(e);
        }

        return calculator;
    }
}
