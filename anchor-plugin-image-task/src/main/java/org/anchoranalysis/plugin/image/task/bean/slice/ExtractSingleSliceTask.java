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

package org.anchoranalysis.plugin.image.task.bean.slice;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.SkipInit;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.experiment.task.Task;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.calc.FeatureCalculationException;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.feature.session.FeatureSession;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.feature.stack.FeatureInputStack;
import org.anchoranalysis.image.io.input.NamedChnlsInput;
import org.anchoranalysis.image.io.stack.StackCollectionOutputter;
import org.anchoranalysis.image.stack.NamedStacks;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.plugin.image.task.imagefeature.calculator.FeatureCalculatorRepeated;
import org.anchoranalysis.plugin.image.task.sharedstate.SharedStateSelectedSlice;

/**
 * Reduces a z-stack to a single-slice by taking the optima of a feature calculated for each slice
 *
 * <p>Expects a second-level output "stack" to determine which slices are outputted or not
 *
 * @author Owen Feehan
 */
public class ExtractSingleSliceTask extends Task<NamedChnlsInput, SharedStateSelectedSlice> {

    private static final String OUTPUT_STACK_KEY = "stack";

    // START BEAN PROPERTIES
    @BeanField @SkipInit @Getter @Setter
    private FeatureListProvider<FeatureInputStack> scoreProvider;

    @BeanField @Getter @Setter private StackProvider nrgStackProvider;

    /** If true, the maxima of the score is searched for. If false, rather the minima. */
    @BeanField @Getter @Setter private boolean findMaxima = true;
    // END BEAN PROPERTIES

    @Override
    public SharedStateSelectedSlice beforeAnyJobIsExecuted(
            BoundOutputManagerRouteErrors outputManager, ParametersExperiment params)
            throws ExperimentExecutionException {
        try {
            return new SharedStateSelectedSlice(outputManager);
        } catch (CreateException e) {
            throw new ExperimentExecutionException(e);
        }
    }

    @Override
    public InputTypesExpected inputTypesExpected() {
        return new InputTypesExpected(NamedChnlsInput.class);
    }

    @Override
    public void doJobOnInputObject(InputBound<NamedChnlsInput, SharedStateSelectedSlice> params)
            throws JobExecutionException {

        try {
            // Create an NRG stack
            NRGStackWithParams nrgStack =
                    FeatureCalculatorRepeated.extractStack(
                            params.getInputObject(), nrgStackProvider, params.context());

            int optimaSliceIndex =
                    selectSlice(
                            nrgStack,
                            params.getLogger(),
                            params.getInputObject().descriptiveName(),
                            params.getSharedState());

            deriveSlicesAndOutput(
                    params.getInputObject(), nrgStack, optimaSliceIndex, params.getOutputManager());

        } catch (OperationFailedException e) {
            throw new JobExecutionException(e);
        }
    }

    @Override
    public void afterAllJobsAreExecuted(
            SharedStateSelectedSlice sharedState, BoundIOContext context)
            throws ExperimentExecutionException {
        sharedState.close();
    }

    /**
     * Returns the index of the selected slice
     *
     * @throws OperationFailedException
     */
    private int selectSlice(
            NRGStackWithParams nrgStack,
            Logger logger,
            String imageName,
            SharedStateSelectedSlice params)
            throws OperationFailedException {

        Feature<FeatureInputStack> scoreFeature = extractScoreFeature();
        try {
            double[] scores = calcScoreForEachSlice(scoreFeature, nrgStack, logger);

            int optimaSliceIndex = findOptimaSlice(scores);

            logger.messageLogger().logFormatted("Selected optima is slice %d", optimaSliceIndex);

            params.writeRow(imageName, optimaSliceIndex, scores[optimaSliceIndex]);

            return optimaSliceIndex;
        } catch (FeatureCalculationException e) {
            throw new OperationFailedException(e);
        }
    }

    private void deriveSlicesAndOutput(
            NamedChnlsInput inputObject,
            NRGStackWithParams nrgStack,
            int optimaSliceIndex,
            BoundOutputManagerRouteErrors outputManager)
            throws OperationFailedException {

        NamedStacks stackCollection = collectionFromInput(inputObject);

        // Extract slices
        NamedStacks sliceCollection =
                stackCollection.applyOperation(
                        nrgStack.getDimensions(), stack -> stack.extractSlice(optimaSliceIndex));

        try {
            outputSlices(outputManager, sliceCollection);
        } catch (OutputWriteFailedException e) {
            throw new OperationFailedException(e);
        }
    }

    private static NamedStacks collectionFromInput(NamedChnlsInput inputObject)
            throws OperationFailedException {
        NamedStacks stackCollection = new NamedStacks();
        inputObject.addToStoreInferNames(stackCollection);
        return stackCollection;
    }

    private void outputSlices(
            BoundOutputManagerRouteErrors outputManager, NamedStacks stackCollection)
            throws OutputWriteFailedException {
        StackCollectionOutputter.outputSubsetWithException(
                stackCollection, outputManager, OUTPUT_STACK_KEY, false);
    }

    private int findOptimaSlice(double[] scores) {
        if (findMaxima) {
            return ArraySearchUtilities.findIndexOfMaximum(scores);
        } else {
            return ArraySearchUtilities.findIndexOfMinimum(scores);
        }
    }

    private double[] calcScoreForEachSlice(
            Feature<FeatureInputStack> scoreFeature, NRGStackWithParams nrgStack, Logger logger)
            throws FeatureCalculationException {
        try {
            FeatureCalculatorSingle<FeatureInputStack> session =
                    FeatureSession.with(scoreFeature, logger);

            double[] results = new double[nrgStack.getDimensions().getZ()];

            // Extract each slice, and calculate feature
            for (int z = 0; z < nrgStack.getDimensions().getZ(); z++) {
                NRGStackWithParams nrgStackSlice = nrgStack.extractSlice(z);

                // Calculate feature for this slice
                double featVal = session.calc(new FeatureInputStack(nrgStackSlice.getNrgStack()));

                logger.messageLogger().logFormatted("Slice %3d has score %f", z, featVal);

                results[z] = featVal;
            }

            return results;

        } catch (InitException | FeatureCalculationException | OperationFailedException e) {
            throw new FeatureCalculationException(e);
        }
    }

    private Feature<FeatureInputStack> extractScoreFeature() throws OperationFailedException {
        try {
            FeatureList<FeatureInputStack> features = scoreProvider.create();
            if (features.size() != 1) {
                throw new OperationFailedException(
                        String.format(
                                "scoreProvider should return a list with exactly 1 feature, instead it has %d features.",
                                features.size()));
            }
            return features.get(0);
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }

    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return false;
    }
}
