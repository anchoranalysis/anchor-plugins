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

package org.anchoranalysis.test.experiment.task;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.time.ExecutionTimeRecorderIgnore;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.arguments.ExecutionArguments;
import org.anchoranalysis.experiment.arguments.TaskArguments;
import org.anchoranalysis.experiment.bean.log.LoggingDestination;
import org.anchoranalysis.experiment.bean.task.Task;
import org.anchoranalysis.experiment.log.StatefulMessageLogger;
import org.anchoranalysis.experiment.task.ExperimentFeedbackContext;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.experiment.task.ParametersUnbound;
import org.anchoranalysis.inference.concurrency.ConcurrencyPlan;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.output.bean.OutputManager;
import org.anchoranalysis.io.output.bean.path.prefixer.PathPrefixer;
import org.anchoranalysis.io.output.bean.rules.NoneExcept;
import org.anchoranalysis.io.output.bean.rules.OutputEnabledRules;
import org.anchoranalysis.io.output.bean.rules.Permissive;
import org.anchoranalysis.io.output.outputter.BindFailedException;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.io.output.outputter.OutputterChecked;
import org.anchoranalysis.test.image.io.BeanInstanceMapFixture;
import org.anchoranalysis.test.image.io.OutputterFixture;
import org.anchoranalysis.test.io.output.OutputManagerFixture;

/**
 * Executes a task on a single-input outputting into a specific directory.
 *
 * @author Owen Feehan
 */
public class ExecuteTaskHelper {

    /**
     * When defined, only this outputs are permitted to occur.
     *
     * <p>Otherwise, all outputs occur as per defaults in the task.
     */
    private final Optional<String> specificOutput;

    /** The arguments to use when executing the task. */
    private final TaskArguments taskArguments;

    /**
     * When true, and two files are not identical, the version in the output directory is copied
     * into the resources directory.
     *
     * <p>Specifically, when the {@code copyNonIdentical} flag is true, and two particular files are
     * deemed to be non-identical, the outputted file in {@code pathDirectoryOutput} will be copied
     * in to replace the existing file in the {@code pathDirectorySaved}. No assertion will be
     * thrown.
     *
     * <p>When false, no copying occurs.
     *
     * <p>This is a powerful tool to update resources when they are out of sync with the tests.
     * However, it should be used very <b>carefully</b> as it overrides existing files in the
     * source-code directory.
     */
    private final boolean copyNonIdentical;

    /** Create so that all outputs occur, and with default {@link TaskArguments}. */
    public ExecuteTaskHelper() {
        this(false);
    }

    /**
     * Create so that all outputs occur, and with default {@link TaskArguments}.
     *
     * @param copyNonIdentical when true, and two files are not identical, the version in the output
     *     directory is copied into the resources directory.
     */
    public ExecuteTaskHelper(boolean copyNonIdentical) {
        this.specificOutput = Optional.empty();
        this.taskArguments = new TaskArguments();
        this.copyNonIdentical = copyNonIdentical;
    }

    /**
     * Create with specific arguments, not necessarily the defaults.
     *
     * @param specificOutput when defined, only this outputs are permitted to occur. Otherwise, all
     *     outputs occur as per defaults in the task.
     * @param taskArguments the arguments to use when executing the task.
     * @param copyNonIdentical when true, and two files are not identical, the version in the output
     *     directory is copied into the resources directory.
     */
    public ExecuteTaskHelper(
            Optional<String> specificOutput,
            TaskArguments taskArguments,
            boolean copyNonIdentical) {
        this.specificOutput = Optional.empty();
        this.taskArguments = new TaskArguments();
        this.copyNonIdentical = copyNonIdentical;
    }

    /**
     * Executes a task on a single-input.
     *
     * <p>Expected outputs exist in a resources directory, and the produced outputs are compared
     * against them, to check that they are identical.
     *
     * @param <T> input type
     * @param <S> shared-state type
     * @param <V> task type
     * @param input the input for the task.
     * @param task the task to run.
     * @param pathDirectoryOutput an absolute path to a directory where outputs of the task will be
     *     placed.
     * @param pathDirectorySaved a path (relative to the {@code src/test/resources}) to a directory
     *     of saved-results to compare with.
     * @param pathsFileToCompare paths (relative to the {@code src/test/resources}) to check that
     *     are identical.
     * @throws OperationFailedException if anything goes wrong.
     */
    public <T extends InputFromManager, S, V extends Task<T, S>> void assertExpectedTaskOutputs(
            T input,
            V task,
            Path pathDirectoryOutput,
            String pathDirectorySaved,
            Iterable<String> pathsFileToCompare)
            throws OperationFailedException {
        assertExpectedTaskOutputs(
                Arrays.asList(input),
                task,
                pathDirectoryOutput,
                pathDirectorySaved,
                pathsFileToCompare);
    }

    /**
     * Executes a task on a multiple inputs and asserts expected outputs - with task arguments.
     *
     * <p>Expected outputs exist in a resources directory, and the produced outputs are compared
     * against them, to check that they are identical.
     *
     * @param <T> input type
     * @param <S> shared-state type
     * @param <V> task type
     * @param inputs the inputs for the task.
     * @param task the task to run.
     * @param pathDirectoryOutput an absolute path to a directory where outputs of the task will be
     *     placed.
     * @param pathDirectorySaved a path (relative to the {@code src/test/resources}) to a directory
     *     of saved-results to compare with.
     * @param pathsFileToCompare paths (relative to the {@code src/test/resources}) to check that
     *     are identical.
     * @throws OperationFailedException if anything goes wrong.
     */
    public <T extends InputFromManager, S, V extends Task<T, S>> void assertExpectedTaskOutputs(
            List<T> inputs,
            V task,
            Path pathDirectoryOutput,
            String pathDirectorySaved,
            Iterable<String> pathsFileToCompare)
            throws OperationFailedException {

        boolean successful =
                executeTaskOnInputs(
                        inputs, task, taskArguments, pathDirectoryOutput, specificOutput);
        // Successful outcome
        assertTrue(successful, "Sucessful execution of task");

        CompareHelper.assertDirectoriesIdentical(
                pathDirectoryOutput, pathDirectorySaved, pathsFileToCompare, copyNonIdentical);
    }

    /**
     * Executes a task on a list of inputs.
     *
     * @param <T> input type
     * @param <S> shared-state type
     * @param <V> task type
     * @param inputs the input for the task.
     * @param task the task to run.
     * @param taskArguments arguments to use for the task.
     * @param pathForOutputs a directory where outputs of the task will be placed.
     * @param specificOutputEnabled if defined, only this output occur. otherwise, all outputs occur
     *     as per defaults in the task.
     * @return true if successful, false otherwise.
     * @throws OperationFailedException if anything goes wrong.
     */
    private static <T extends InputFromManager, S, V extends Task<T, S>>
            boolean executeTaskOnInputs(
                    List<T> inputs,
                    V task,
                    TaskArguments taskArguments,
                    Path pathForOutputs,
                    Optional<String> specificOutputEnabled)
                    throws OperationFailedException {

        try {
            BeanInstanceMapFixture.check(task);

            OutputEnabledRules outputEnabled = createOutputRules(specificOutputEnabled);

            OutputManager outputManager =
                    OutputManagerFixture.createOutputManager(
                            Optional.of(pathForOutputs), Optional.of(outputEnabled));

            Outputter outputter = OutputterFixture.outputter(outputManager, outputEnabled);

            StatefulMessageLogger logger = createStatefulLogReporter();

            ParametersExperiment parametersExperiment =
                    createParametersExperiment(
                            outputter.getChecked(),
                            outputManager.getPrefixer(),
                            taskArguments,
                            logger);

            ConcurrencyPlan concurrencyPlan = ConcurrencyPlan.singleCPUProcessor(0);
            S sharedState =
                    task.beforeAnyJobIsExecuted(
                            outputter, concurrencyPlan, inputs, parametersExperiment);

            try {
                boolean successful = true;
                for (T input : inputs) {
                    successful =
                            successful
                                    && task.executeJob(
                                            new ParametersUnbound<>(
                                                    parametersExperiment,
                                                    input,
                                                    sharedState,
                                                    false));
                }
                return successful;

            } finally {
                task.afterAllJobsAreExecuted(sharedState, parametersExperiment.getContext());
            }

        } catch (ExperimentExecutionException | JobExecutionException | BindFailedException exc) {
            throw new OperationFailedException(exc);
        }
    }

    private static OutputEnabledRules createOutputRules(Optional<String> specificOutputEnabled) {
        if (specificOutputEnabled.isPresent()) {
            return specificOutputEnabled.map(NoneExcept::new).get();
        } else {
            return new Permissive();
        }
    }

    private static ParametersExperiment createParametersExperiment(
            OutputterChecked outputter,
            PathPrefixer prefixer,
            TaskArguments taskArguments,
            StatefulMessageLogger logger) {
        ExperimentFeedbackContext context =
                new ExperimentFeedbackContext(
                        logger, false, ExecutionTimeRecorderIgnore.instance());
        ParametersExperiment parameters =
                new ParametersExperiment(
                        new ExecutionArguments(Paths.get("."), taskArguments),
                        "arbitraryExperimentName",
                        outputter,
                        prefixer,
                        context);
        parameters.setLoggerTaskCreator(createLogReporterBean());
        return parameters;
    }

    private static LoggingDestination createLogReporterBean() {
        LoggingDestination logger = mock(LoggingDestination.class);
        when(logger.create(any(), any(), any(), anyBoolean()))
                .thenReturn(createStatefulLogReporter());
        when(logger.createWithLogFallback(any(), any(), any(), anyBoolean()))
                .thenReturn(createStatefulLogReporter());
        return logger;
    }

    private static StatefulMessageLogger createStatefulLogReporter() {
        return mock(StatefulMessageLogger.class);
    }
}
