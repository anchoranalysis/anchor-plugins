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

import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.SkipInit;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.bean.task.Task;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.bound.FeatureCalculatorSingle;
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.feature.session.FeatureSession;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.core.stack.named.NamedStacks;
import org.anchoranalysis.image.feature.input.FeatureInputStack;
import org.anchoranalysis.image.io.channel.input.NamedChannelsInput;
import org.anchoranalysis.image.io.stack.output.NamedStacksOutputter;
import org.anchoranalysis.inference.concurrency.ConcurrencyPlan;
import org.anchoranalysis.io.output.enabled.OutputEnabledMutable;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.io.output.outputter.OutputterChecked;
import org.anchoranalysis.plugin.image.task.feature.calculator.FeatureCalculatorRepeated;
import org.anchoranalysis.plugin.image.task.stack.SharedStateSelectedSlice;

/**
 * Reduces a z-stack to a single-slice by taking the optima of a feature calculated for each slice.
 *
 * <p>Expects a second-level output "stack" to determine which slices are outputted or not.
 *
 * @author Owen Feehan
 */
public class ExtractSlice extends Task<NamedChannelsInput, SharedStateSelectedSlice> {

    private static final String OUTPUT_SLICES = "slices";

    // START BEAN PROPERTIES
    @BeanField @SkipInit @Getter @Setter
    private FeatureListProvider<FeatureInputStack> scoreProvider;

    @BeanField @Getter @Setter private StackProvider stackEnergy;

    /** If true, the maxima of the score is searched for. If false, rather the minima. */
    @BeanField @Getter @Setter private boolean findMaxima = true;

    // END BEAN PROPERTIES

    @Override
    public SharedStateSelectedSlice beforeAnyJobIsExecuted(
            Outputter outputter,
            ConcurrencyPlan concurrencyPlan,
            List<NamedChannelsInput> inputs,
            ParametersExperiment parameters)
            throws ExperimentExecutionException {
        try {
            return new SharedStateSelectedSlice(outputter);
        } catch (CreateException e) {
            throw new ExperimentExecutionException(e);
        }
    }

    @Override
    public InputTypesExpected inputTypesExpected() {
        return new InputTypesExpected(NamedChannelsInput.class);
    }

    @Override
    public void doJobOnInput(InputBound<NamedChannelsInput, SharedStateSelectedSlice> input)
            throws JobExecutionException {

        try {
            // Create an energy stack
            EnergyStack energyStack =
                    FeatureCalculatorRepeated.extractStack(
                            input.getInput(), stackEnergy, input.getContextJob());

            int optimaSliceIndex =
                    selectSlice(
                            energyStack,
                            input.getLogger(),
                            input.getInput().identifier(),
                            input.getSharedState());

            deriveSlicesAndOutput(
                    input.getInput(),
                    energyStack,
                    optimaSliceIndex,
                    input.getOutputter().getChecked(),
                    input.getContextJob().getLogger());

        } catch (OperationFailedException e) {
            throw new JobExecutionException(e);
        }
    }

    @Override
    public void afterAllJobsAreExecuted(
            SharedStateSelectedSlice sharedState, InputOutputContext context)
            throws ExperimentExecutionException {
        sharedState.close();
    }

    @Override
    public OutputEnabledMutable defaultOutputs() {
        return super.defaultOutputs().addEnabledOutputFirst(OUTPUT_SLICES);
    }

    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return false;
    }

    /**
     * Returns the index of the selected slice
     *
     * @throws OperationFailedException
     */
    private int selectSlice(
            EnergyStack energyStack,
            Logger logger,
            String imageName,
            SharedStateSelectedSlice sharedState)
            throws OperationFailedException {

        Feature<FeatureInputStack> scoreFeature = extractScoreFeature();
        try {
            double[] scores = calculateScoreForEachSlice(scoreFeature, energyStack, logger);

            int optimalSliceIndex = findOptimalSlice(scores);

            logger.messageLogger().logFormatted("Selected optima is slice %d", optimalSliceIndex);

            sharedState.writeRow(imageName, optimalSliceIndex, scores[optimalSliceIndex]);

            return optimalSliceIndex;
        } catch (FeatureCalculationException e) {
            throw new OperationFailedException(e);
        }
    }

    private void deriveSlicesAndOutput(
            NamedChannelsInput input,
            EnergyStack energyStack,
            int optimaSliceIndex,
            OutputterChecked outputter,
            Logger logger)
            throws OperationFailedException {

        NamedStacks stacks = collectionFromInput(input, logger);

        // Extract slices
        NamedStacks slices =
                stacks.applyOperation(
                        stack -> stack.extractSlice(optimaSliceIndex),
                        Optional.of(energyStack.dimensions()));

        try {
            NamedStacksOutputter.output(slices, OUTPUT_SLICES, false, outputter);
        } catch (OutputWriteFailedException e) {
            throw new OperationFailedException(e);
        }
    }

    private static NamedStacks collectionFromInput(NamedChannelsInput input, Logger logger)
            throws OperationFailedException {
        NamedStacks stackCollection = new NamedStacks();
        input.addToStoreInferNames(stackCollection, logger);
        return stackCollection;
    }

    private int findOptimalSlice(double[] scores) {
        if (findMaxima) {
            return ArraySearchUtilities.findIndexOfMaximum(scores);
        } else {
            return ArraySearchUtilities.findIndexOfMinimum(scores);
        }
    }

    private double[] calculateScoreForEachSlice(
            Feature<FeatureInputStack> scoreFeature, EnergyStack energyStack, Logger logger)
            throws FeatureCalculationException {
        try {
            FeatureCalculatorSingle<FeatureInputStack> session =
                    FeatureSession.with(scoreFeature, logger);

            double[] results = new double[energyStack.dimensions().z()];

            // Extract each slice, and calculate feature
            for (int z = 0; z < energyStack.dimensions().z(); z++) {
                EnergyStack energyStackSlice = energyStack.extractSlice(z);

                // Calculate feature for this slice
                double featVal =
                        session.calculate(
                                new FeatureInputStack(energyStackSlice.withoutParameters()));

                logger.messageLogger().logFormatted("Slice %3d has score %f", z, featVal);

                results[z] = featVal;
            }

            return results;

        } catch (InitializeException | FeatureCalculationException | OperationFailedException e) {
            throw new FeatureCalculationException(e);
        }
    }

    private Feature<FeatureInputStack> extractScoreFeature() throws OperationFailedException {
        try {
            FeatureList<FeatureInputStack> features = scoreProvider.get();
            if (features.size() != 1) {
                throw new OperationFailedException(
                        String.format(
                                "scoreProvider should return a list with exactly 1 feature, instead it has %d features.",
                                features.size()));
            }
            return features.get(0);
        } catch (ProvisionFailedException e) {
            throw new OperationFailedException(e);
        }
    }
}
