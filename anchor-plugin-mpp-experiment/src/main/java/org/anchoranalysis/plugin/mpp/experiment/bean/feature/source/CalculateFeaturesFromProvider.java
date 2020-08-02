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

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.core.functional.function.FunctionWithException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.feature.calc.NamedFeatureCalculationException;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.io.csv.StringLabelsForCsvRow;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorMulti;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.feature.session.FeatureTableCalculator;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.plugin.image.feature.bean.object.combine.CombineObjectsForFeatures;
import org.anchoranalysis.plugin.image.task.feature.InputProcessContext;
import org.anchoranalysis.plugin.image.task.feature.ResultsVectorWithThumbnail;
import org.anchoranalysis.plugin.mpp.experiment.feature.source.InitParamsWithNrgStack;
import lombok.AllArgsConstructor;

@AllArgsConstructor
class CalculateFeaturesFromProvider<T extends FeatureInput> {

    private final CombineObjectsForFeatures<T> table;
    private final FeatureCalculatorMulti<T> calculator;
    private final InitParamsWithNrgStack initParams;
    
    /** iff TRUE no exceptions are thrown when an error occurs, but rather a message is written to the log */
    private final boolean suppressErrors;
    
    /** Generates thumbnails (if enabled) */
    private final Optional<FunctionWithException<T,Optional<DisplayStack>,CreateException>> thumbnailForInput;
    
    private final InputProcessContext<FeatureTableCalculator<T>> context;

    public void processProvider(
            ObjectCollectionProvider provider,
            Function<T, StringLabelsForCsvRow> identifierFromInput)
            throws OperationFailedException {
        calculateFeaturesForProvider(
                objectsFromProvider(provider, initParams.getImageInit(), context.getLogger()),
                initParams.getNrgStack(),
                identifierFromInput);
    }

    private void calculateFeaturesForProvider(
            ObjectCollection objects,
            NRGStackWithParams nrgStack,
            Function<T, StringLabelsForCsvRow> identifierFromInput)
            throws OperationFailedException {
        try {
            List<T> inputs = table.deriveInputs(objects, nrgStack, thumbnailForInput.isPresent(), context.getLogger());

            calculateManyFeaturesInto(inputs, identifierFromInput);
            
            table.endBatchAndCleanup();
            
        } catch (CreateException | OperationFailedException e) {
            throw new OperationFailedException(e);
        }
    }

    /**
     * Calculates a bunch of features with an objectID (unique) and a groupID and adds them to the
     * stored-results
     *
     * <p>The stored-results also have an additional first-column with the ID.
     *
     * @param session for calculating features
     * @param listInputs a list of parameters. Each parameters creates a new result (e.g. a new row
     *     in a feature-table)
     * @throws OperationFailedException
     */
    private void calculateManyFeaturesInto(
            List<T> listInputs,
            Function<T, StringLabelsForCsvRow> labelsForInput)
            throws OperationFailedException {

        try {
            for (int i = 0; i < listInputs.size(); i++) {

                T input = listInputs.get(i);

                context.getLogger().messageLogger()
                        .logFormatted(
                                "Calculating input %d of %d: %s",
                                i + 1, listInputs.size(), input.toString());

                context.addResultsFor(
                        labelsForInput.apply(input),
                        new ResultsVectorWithThumbnail(
                           calculator.calc(input, context.getLogger().errorReporter(), suppressErrors),
                           OptionalUtilities.flatMap( thumbnailForInput, func->func.apply(input) )
                        )
                );
            }

        } catch (NamedFeatureCalculationException | CreateException e) {
            throw new OperationFailedException(e);
        }
    }

    private static ObjectCollection objectsFromProvider(
            ObjectCollectionProvider provider, ImageInitParams imageInitParams, Logger logger)
            throws OperationFailedException {

        try {
            ObjectCollectionProvider providerDuplicated = provider.duplicateBean();

            // Initialise
            providerDuplicated.initRecursive(imageInitParams, logger);

            return providerDuplicated.create();

        } catch (InitException | CreateException e) {
            throw new OperationFailedException(e);
        }
    }
}
