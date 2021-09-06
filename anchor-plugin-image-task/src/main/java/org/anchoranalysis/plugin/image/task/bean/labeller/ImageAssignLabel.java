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

import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.annotation.SkipInit;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.concurrency.ConcurrencyPlan;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.InitException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.bean.task.Task;
import org.anchoranalysis.experiment.io.InitializationContext;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.io.stack.input.ProvidesStackInput;
import org.anchoranalysis.image.io.stack.output.generator.StackGenerator;
import org.anchoranalysis.io.output.enabled.OutputEnabledMutable;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.plugin.image.task.labeller.SharedStateFilteredImageOutput;
import org.anchoranalysis.plugin.image.task.stack.InitializationFactory;

/**
 * Assigns a label to each image and copies into subdirectories for each label, and creates a
 * labelling CSV.
 *
 * <p>The following outputs are produced:
 *
 * <table>
 * <caption></caption>
 * <thead>
 * <tr><th>Output Name</th><th>Default?</th><th>Description</th></tr>
 * </thead>
 * <tbody>
 * <tr><td>images</td><td>yes</td><td>a directory where copies of input images are placed in subdirectories corresponding to their label.</td></tr>
 * <tr><td>mapping</td><td>yes</td><td>a single CSV file where each row is an image-label correspondence.</td></tr>
 * <tr><td rowspan="3"><i>inherited from {@link Task}</i></td></tr>
 * </tbody>
 * </table>
 *
 * @author Owen Feehan
 * @param <T> type of init-params associated with the filter
 */
public class ImageAssignLabel<T>
        extends Task<ProvidesStackInput, SharedStateFilteredImageOutput<T>> {

    /** Output-name for a single CSV file where each row is an image-label correspondence. */
    private static final String OUTPUT_MAPPING = "mapping";

    /**
     * Output-name (and directory) for where copies of the input images are placed in
     * sub-directories corresponding to their label.
     */
    private static final String OUTPUT_IMAGES = "images";

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
            Outputter outputter,
            ConcurrencyPlan concurrencyPlan,
            List<ProvidesStackInput> inputs,
            ParametersExperiment params)
            throws ExperimentExecutionException {
        try {
            return new SharedStateFilteredImageOutput<>(
                    outputter, imageLabeller, OUTPUT_MAPPING, OUTPUT_IMAGES);
        } catch (CreateException e) {
            throw new ExperimentExecutionException(e);
        }
    }

    @Override
    public OutputEnabledMutable defaultOutputs() {
        return super.defaultOutputs().addEnabledOutputFirst(OUTPUT_MAPPING, OUTPUT_IMAGES);
    }

    @Override
    public InputTypesExpected inputTypesExpected() {
        return new InputTypesExpected(ProvidesStackInput.class);
    }

    @Override
    public void doJobOnInput(
            InputBound<ProvidesStackInput, SharedStateFilteredImageOutput<T>> input)
            throws JobExecutionException {

        try {
            String groupIdentifier =
                    input.getSharedState().labelFor(input.getInput(), input.getContextJob());

            input.getSharedState().writeRow(input.getInput().identifier(), groupIdentifier);

            if (outputStackProvider != null) {
                outputStack(
                        groupIdentifier,
                        createFromProviderWith(
                                outputStackProvider,
                                input.getInput(),
                                input.createInitializationContext()),
                        input.getInput().identifier(),
                        input.getSharedState());
            }
        } catch (OperationFailedException | CreateException e) {
            throw new JobExecutionException(e);
        }
    }

    private static Stack createFromProviderWith(
            StackProvider provider, ProvidesStackInput stack, InitializationContext context)
            throws CreateException {
        try {
            provider.initRecursive(
                    InitializationFactory.createWithStacks(stack, context), context.getLogger());
            return provider.get();
        } catch (OperationFailedException | ProvisionFailedException | InitException e) {
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

        Optional<Outputter> outputter = sharedState.getOutputterFor(groupIdentifier);
        // Copies the file into the output
        if (outputter.isPresent()) {
            outputter
                    .get()
                    .writerSelective()
                    .write(
                            outputName,
                            () -> new StackGenerator(true, Optional.of("raster"), false),
                            () -> stack);
        }
    }
}
