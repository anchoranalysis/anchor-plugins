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

package org.anchoranalysis.plugin.mpp.experiment.bean;

import java.util.function.Function;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.concurrency.ConcurrencyPlan;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.bean.processor.JobProcessor;
import org.anchoranalysis.experiment.bean.task.Task;
import org.anchoranalysis.experiment.io.ReplaceTask;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.image.io.input.NamedChannelsInput;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.output.enabled.OutputEnabledMutable;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.mpp.io.input.MultiInput;
import org.anchoranalysis.plugin.io.bean.input.stack.ConvertChannelsInputToStack;
import org.anchoranalysis.plugin.io.bean.input.stack.StackSequenceInput;

/**
 * Converts {@link NamedChannelsInput} to a variety of others to match a delegate task
 *
 * <p>Note that the presence of {@link ReplaceTask} gives special behavior to this task in the
 * {@link JobProcessor}
 *
 * @author Owen Feehan
 * @param <T> the named-channels-input we expect to receive
 * @param <S> shared-state of the task
 * @param <U> the named-channels-input the delegate task contains
 */
public class ConvertNamedChannels<T extends NamedChannelsInput, S, U extends NamedChannelsInput>
        extends Task<T, S> implements ReplaceTask<U, S> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private Task<U, S> task;
    // END BEAN PROPERTIES

    @Override
    public S beforeAnyJobIsExecuted(
            Outputter outputter, ConcurrencyPlan concurrencyPlan, ParametersExperiment params)
            throws ExperimentExecutionException {
        return task.beforeAnyJobIsExecuted(outputter, concurrencyPlan, params);
    }

    @Override
    public void doJobOnInput(InputBound<T, S> inputBound) throws JobExecutionException {

        Class<? extends InputFromManager> inputClass = inputBound.getInput().getClass();

        InputTypesExpected expectedFromDelegate = task.inputTypesExpected();
        if (expectedFromDelegate.doesClassInheritFromAny(inputClass)) {
            // All good, the delegate happily accepts our type without change
            doJobWithNamedChannelInput(inputBound);
        } else if (expectedFromDelegate.doesClassInheritFromAny(MultiInput.class)) {
            doJobWithMultiInput(inputBound);
        } else if (expectedFromDelegate.doesClassInheritFromAny(StackSequenceInput.class)) {
            doJobWithStackSequenceInput(inputBound);
        } else {
            throw new JobExecutionException(
                    String.format(
                            "Cannot pass or convert the input-type (%s) to match the delegate's expected input-type:%n%s",
                            inputClass, expectedFromDelegate));
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
    public void afterAllJobsAreExecuted(S sharedState, InputOutputContext context)
            throws ExperimentExecutionException {
        task.afterAllJobsAreExecuted(sharedState, context);
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

    private void doJobWithNamedChannelInput(InputBound<T, S> input) throws JobExecutionException {
        doJobWithInputCast(input);
    }

    private void doJobWithMultiInput(InputBound<T, S> input) throws JobExecutionException {
        doJobWithConvertedInput(input, MultiInput::new);
    }

    private void doJobWithStackSequenceInput(InputBound<T, S> input) throws JobExecutionException {
        doJobWithConvertedInput(input, ConvertChannelsInputToStack::new);
    }

    private void doJobWithConvertedInput(
            InputBound<T, S> input, Function<T, InputFromManager> deriveChangedInput)
            throws JobExecutionException {
        doJobWithInputCast(input.changeInput(deriveChangedInput.apply(input.getInput())));
    }

    @SuppressWarnings("unchecked")
    private void doJobWithInputCast(InputBound<? extends InputFromManager, S> input)
            throws JobExecutionException {
        task.doJobOnInput((InputBound<U, S>) input);
    }
}
