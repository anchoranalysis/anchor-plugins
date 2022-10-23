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
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExecuteTaskHelper {

    /**
     * Executes a task on a single-input.
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
    public static <T extends InputFromManager, S, V extends Task<T, S>>
            void runTaskAndCompareOutputs(
                    T input,
                    V task,
                    Path pathDirectoryOutput,
                    String pathDirectorySaved,
                    Iterable<String> pathsFileToCompare)
                    throws OperationFailedException {
        runTaskAndCompareOutputs(
                Arrays.asList(input),
                task,
                pathDirectoryOutput,
                pathDirectorySaved,
                pathsFileToCompare);
    }

    /**
     * Executes a task on a multiple inputs.
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
    public static <T extends InputFromManager, S, V extends Task<T, S>>
            void runTaskAndCompareOutputs(
                    List<T> inputs,
                    V task,
                    Path pathDirectoryOutput,
                    String pathDirectorySaved,
                    Iterable<String> pathsFileToCompare)
                    throws OperationFailedException {
        runTaskAndCompareOutputs(
                inputs,
                true,
                task,
                new TaskArguments(),
                pathDirectoryOutput,
                Optional.empty(),
                pathDirectorySaved,
                pathsFileToCompare);
    }

    /**
     * Executes a task on a multiple inputs - with task arguments.
     *
     * @param <T> input type
     * @param <S> shared-state type
     * @param <V> task type
     * @param inputs the inputs for the task.
     * @param prefixEachInput when true, a prefix is assigned representing each input into the
     *     output path to them unique. When false, this doesn't occur, and output path is constant
     *     for all inputs. The latter should only be used when there is a single input, or if we are
     *     only interested in outputs that are are singularly outputted from the task as a whole
     *     (but not per individual input).
     * @param task the task to run.
     * @param taskArguments arguments for the task.
     * @param pathDirectoryOutput an absolute path to a directory where outputs of the task will be
     *     placed.
     * @param pathDirectorySaved a path (relative to the {@code src/test/resources}) to a directory
     *     of saved-results to compare with.
     * @param pathsFileToCompare paths (relative to the {@code src/test/resources}) to check that
     *     are identical.
     * @throws OperationFailedException if anything goes wrong.
     */
    public static <T extends InputFromManager, S, V extends Task<T, S>>
            void runTaskAndCompareOutputs(
                    List<T> inputs,
                    boolean prefixEachInput,
                    V task,
                    TaskArguments taskArguments,
                    Path pathDirectoryOutput,
                    Optional<String> specificOutput,
                    String pathDirectorySaved,
                    Iterable<String> pathsFileToCompare)
                    throws OperationFailedException {

        boolean successful =
                runTaskOnInputs(
                        inputs,
                        prefixEachInput,
                        task,
                        taskArguments,
                        pathDirectoryOutput,
                        specificOutput);
        // Successful outcome
        assertTrue(successful, "Sucessful execution of task");

        CompareHelper.compareOutputWithSaved(
                pathDirectoryOutput, pathDirectorySaved, pathsFileToCompare);
    }

    /**
     * Executes a task on a list of inputs.
     *
     * @param <T> input type
     * @param <S> shared-state type
     * @param <V> task type
     * @param inputs the input for the task.
     * @param prefixEachInput when true, a prefix is assigned representing each input into the
     *     output path to them unique. When false, this doesn't occur, and output path is constant
     *     for all inputs. The latter should only be used when there is a single input.
     * @param task the task to run.
     * @param taskArguments arguments to use for the task.
     * @param pathForOutputs a directory where outputs of the task will be placed.
     * @param specificOutputEnabled if defined, only this output occur. otherwise, all outputs occur
     *     as per defaults in the task.
     * @return true if successful, false otherwise.
     * @throws OperationFailedException if anything goes wrong.
     */
    private static <T extends InputFromManager, S, V extends Task<T, S>> boolean runTaskOnInputs(
            List<T> inputs,
            boolean prefixEachInput,
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
                            Optional.of(pathForOutputs),
                            prefixEachInput,
                            Optional.of(outputEnabled));

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
