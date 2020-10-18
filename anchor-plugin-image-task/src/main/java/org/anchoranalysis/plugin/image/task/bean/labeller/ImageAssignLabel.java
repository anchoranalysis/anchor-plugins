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

package org.anchoranalysis.plugin.image.task.bean.labeller;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.annotation.SkipInit;
import org.anchoranalysis.core.concurrency.ConcurrencyPlan;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.bean.task.Task;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.io.generator.raster.StackGenerator;
import org.anchoranalysis.image.io.input.ProvidesStackInput;
import org.anchoranalysis.image.io.input.StackInputInitParamsCreator;
import org.anchoranalysis.io.output.enabled.OutputEnabledMutable;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.plugin.image.task.labeller.SharedStateFilteredImageOutput;

/**
 * Assigns a label to each image and optionally 1. copies each image into directory corresponding to
 * the label (e.g. "positive", "negative) 2. creates a single CSV file where each row is an
 * image-label correspondence
 *
 * @author Owen Feehan
 * @param <T> type of init-params associated with the filter
 */
public class ImageAssignLabel<T>
        extends Task<ProvidesStackInput, SharedStateFilteredImageOutput<T>> {

    // START BEAN PROPERTIES
    /** Maps a label to an image */
    @BeanField @Getter @Setter private ImageLabeller<T> imageLabeller;

    /**
     * If it's set, a stack is generated that is outputted into subdirectory corresponding to the
     * groupIdentifier.
     */
    @BeanField @OptionalBean @SkipInit @Getter @Setter private StackProvider outputStackProvider;
    // END BEAN PROPERTIES

    @Override
    public SharedStateFilteredImageOutput<T> beforeAnyJobIsExecuted(
            Outputter outputter, ConcurrencyPlan concurrencyPlan, ParametersExperiment params)
            throws ExperimentExecutionException {
        try {
            return new SharedStateFilteredImageOutput<>(outputter, imageLabeller);
        } catch (CreateException e) {
            throw new ExperimentExecutionException(e);
        }
    }

    @Override
    public OutputEnabledMutable defaultOutputs() {
        assert (false);
        return super.defaultOutputs();
    }

    @Override
    public InputTypesExpected inputTypesExpected() {
        return new InputTypesExpected(ProvidesStackInput.class);
    }

    @Override
    public void doJobOnInput(
            InputBound<ProvidesStackInput, SharedStateFilteredImageOutput<T>> params)
            throws JobExecutionException {

        try {
            String groupIdentifier =
                    params.getSharedState().labelFor(params.getInput(), params.context());

            params.getSharedState().writeRow(params.getInput().name(), groupIdentifier);

            if (outputStackProvider != null) {
                outputStack(
                        groupIdentifier,
                        createFromProviderWith(
                                outputStackProvider, params.getInput(), params.context()),
                        params.getInput().name(),
                        params.getSharedState());
            }
        } catch (OperationFailedException | CreateException e) {
            throw new JobExecutionException(e);
        }
    }

    private static Stack createFromProviderWith(
            StackProvider provider, ProvidesStackInput stack, InputOutputContext context)
            throws CreateException {
        try {
            provider.initRecursive(
                    StackInputInitParamsCreator.createInitParams(stack, context),
                    context.getLogger());
            return provider.create();
        } catch (InitException | OperationFailedException e) {
            throw new CreateException(e);
        }
    }

    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return false;
    }

    @Override
    public void afterAllJobsAreExecuted(
            SharedStateFilteredImageOutput<T> sharedState, InputOutputContext context)
            throws ExperimentExecutionException {
        sharedState.close();
    }

    private void outputStack(
            String groupIdentifier,
            Stack stack,
            String outputName,
            SharedStateFilteredImageOutput<T> sharedState) {

        // Copies the file into the output
        sharedState
                .getOutputterFor(groupIdentifier)
                .writerPermissive()
                .write(
                        outputName,
                        () -> new StackGenerator(true, Optional.of("raster"), false),
                        () -> stack);
    }
}
