/*-
 * #%L
 * anchor-mpp-io
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

package org.anchoranalysis.plugin.mpp.experiment.bean.convert;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.OptionalFactory;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.bean.processor.JobProcessor;
import org.anchoranalysis.experiment.bean.task.Task;
import org.anchoranalysis.experiment.io.ReplaceTask;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.image.io.channel.input.NamedChannelsInput;
import org.anchoranalysis.inference.concurrency.ConcurrencyPlan;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.input.file.FileWithDirectoryInput;
import org.anchoranalysis.io.output.enabled.OutputEnabledMutable;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.mpp.io.input.MultiInput;
import org.anchoranalysis.plugin.mpp.experiment.shared.SharedStateRememberConverted;

/**
 * Converts {@link NamedChannelsInput} to a variety of others to match a delegate task.
 *
 * <p>Note that the presence of {@link ReplaceTask} gives special behavior to this task in the
 * {@link JobProcessor}
 *
 * @author Owen Feehan
 * @param <T> the named-channels-input we expect to receive
 * @param <S> shared-state of the task
 * @param <U> the named-channels-input the delegate task contains
 */
public class ConvertNamedChannels<T extends NamedChannelsInput, S, U extends InputFromManager>
        extends Task<T, SharedStateRememberConverted<U, S>> implements ReplaceTask<U, S> {

    // START BEAN PROPERTIES
    /** The underlying task that will be called, perhaps with a different input-type. */
    @BeanField @Getter @Setter private Task<U, S> task;
    // END BEAN PROPERTIES

    @Override
    public SharedStateRememberConverted<U, S> beforeAnyJobIsExecuted(
            Outputter outputter,
            ConcurrencyPlan concurrencyPlan,
            List<T> inputs,
            ParametersExperiment params)
            throws ExperimentExecutionException {

        SharedStateRememberConverted<U, S> sharedState = new SharedStateRememberConverted<>();
        List<U> convertedInputs = convertListAndPopulateMap(inputs, sharedState);
        sharedState.setSharedState(
                task.beforeAnyJobIsExecuted(outputter, concurrencyPlan, convertedInputs, params));
        return sharedState;
    }

    @Override
    public void doJobOnInput(InputBound<T, SharedStateRememberConverted<U, S>> inputBound)
            throws JobExecutionException {

        Optional<U> inputConverted =
                inputBound.getSharedState().findConvertedInputFor(inputBound.getInput());

        if (inputConverted.isPresent()) {
            task.doJobOnInput(
                    inputBound.changeInputAndSharedState(
                            inputConverted.get(), inputBound.getSharedState().getSharedState()));
        } else {
            throw new JobExecutionException("No converted-input can be found in the map");
        }
    }

    @Override
    public InputTypesExpected inputTypesExpected() {
        InputTypesExpected expected = new InputTypesExpected(NamedChannelsInput.class);
        // Add the other types we'll consider converting
        expected.add(MultiInput.class);
        return expected;
    }

    @Override
    public void afterAllJobsAreExecuted(
            SharedStateRememberConverted<U, S> sharedState, InputOutputContext context)
            throws ExperimentExecutionException {
        sharedState.forgetAll();
        task.afterAllJobsAreExecuted(sharedState.getSharedState(), context);
    }

    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return task.hasVeryQuickPerInputExecution();
    }

    @Override
    public void replaceTask(Task<U, S> taskToReplace) throws OperationFailedException {
        this.task = taskToReplace;
    }

    @Override
    public OutputEnabledMutable defaultOutputs() {
        return task.defaultOutputs();
    }

    /** Convert all inputs, placing them into the map. */
    @SuppressWarnings("unchecked")
    private List<U> convertListAndPopulateMap(
            List<T> inputs, SharedStateRememberConverted<U, S> sharedState)
            throws ExperimentExecutionException {

        Optional<Path> directory = deriveCommonRootIfNeeded(inputs);

        assert (!directory.isPresent() || directory.get().toFile().isDirectory());

        List<U> out = new ArrayList<>();

        InputTypesExpected inputTypesExpected = task.inputTypesExpected();

        for (T input : inputs) {
            // Convert and put in both the map and the list
            U converted = (U) ConvertInputHelper.convert(input, inputTypesExpected, directory);
            sharedState.rememberConverted(input, converted);
            out.add(converted);
        }
        return out;
    }

    /**
     * If a directory is associated with the inputs, as needed for conversion to {@link
     * FileWithDirectoryInput}.
     */
    private Optional<Path> deriveCommonRootIfNeeded(List<T> inputs)
            throws ExperimentExecutionException {
        // Derive a directory if needed
        InputTypesExpected expectedFromDelegate = task.inputTypesExpected();
        return OptionalFactory.createWithException(
                expectedFromDelegate.doesClassInheritFromAny(FileWithDirectoryInput.class),
                () -> CommonRootHelper.findCommonPathRoot(inputs));
    }
}