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
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.bean.exception.BeanMisconfiguredException;
import org.anchoranalysis.bean.xml.RegisterBeanFactories;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.arguments.ExecutionArguments;
import org.anchoranalysis.experiment.bean.log.LoggingDestination;
import org.anchoranalysis.experiment.bean.task.Task;
import org.anchoranalysis.experiment.log.StatefulMessageLogger;
import org.anchoranalysis.experiment.task.ExecutionTimeStatistics;
import org.anchoranalysis.experiment.task.ExperimentFeedbackContext;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.experiment.task.ParametersUnbound;
import org.anchoranalysis.inference.concurrency.ConcurrencyPlan;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.output.bean.OutputManager;
import org.anchoranalysis.io.output.bean.path.prefixer.PathPrefixer;
import org.anchoranalysis.io.output.outputter.BindFailedException;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.io.output.outputter.OutputterChecked;
import org.anchoranalysis.test.image.io.OutputManagerForImagesFixture;
import org.anchoranalysis.test.image.io.OutputterFixture;

/**
 * Executes a task on a single-input outputting into a specific directory.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TaskSingleInputHelper {

    /**
     * Executes a task on a single-input.
     *
     * @param <T> input type
     * @param <S> shared-state type
     * @param <V> task type
     * @param input the input for the task
     * @param task the task to run
     * @param pathDirectoryOutput an absolute path to a directory where outputs of the task will be
     *     placed
     * @param pathDirectorySaved a path (relative to the {@code src/test/resources}) to a directory
     *     of saved-results to compare with
     * @param pathsFileToCompare paths (relative to the {@code src/test/resources}) to check that
     *     are identical
     * @throws OperationFailedException if anything goes wrong
     */
    public static <T extends InputFromManager, S, V extends Task<T, S>>
            void runTaskAndCompareOutputs(
                    T input,
                    V task,
                    Path pathDirectoryOutput,
                    String pathDirectorySaved,
                    Iterable<String> pathsFileToCompare)
                    throws OperationFailedException {

        boolean successful = runTaskOnSingleInput(input, task, pathDirectoryOutput);
        // Successful outcome
        assertTrue(successful, "Sucessful execution of task");

        CompareHelper.compareOutputWithSaved(
                pathDirectoryOutput, pathDirectorySaved, pathsFileToCompare);
    }

    /**
     * Executes a task on a single-input.
     *
     * @param <T> input type
     * @param <S> shared-state type
     * @param <V> task type
     * @param input the input for the task
     * @param task the task to run
     * @param pathForOutputs a directory where outputs of the task will be placed
     * @return true if successful, false otherwise
     * @throws OperationFailedException if anything goes wrong
     */
    private static <T extends InputFromManager, S, V extends Task<T, S>>
            boolean runTaskOnSingleInput(T input, V task, Path pathForOutputs)
                    throws OperationFailedException {

        try {
            task.checkMisconfigured(RegisterBeanFactories.getDefaultInstances());

            OutputManager outputManager =
                    OutputManagerForImagesFixture.createOutputManager(Optional.of(pathForOutputs));

            Outputter outputter = OutputterFixture.outputter(outputManager);

            StatefulMessageLogger logger = createStatefulLogReporter();

            ParametersExperiment paramsExperiment =
                    createParametersExperiment(
                            outputter.getChecked(), outputManager.getPrefixer(), logger);

            ConcurrencyPlan concurrencyPlan = ConcurrencyPlan.singleCPUProcessor(0);
            S sharedState =
                    task.beforeAnyJobIsExecuted(
                            outputter, concurrencyPlan, Arrays.asList(input), paramsExperiment);

            try {
                return task.executeJob(
                        new ParametersUnbound<>(paramsExperiment, input, sharedState, false));
            } finally {
                task.afterAllJobsAreExecuted(sharedState, paramsExperiment.getContext());
            }

        } catch (ExperimentExecutionException
                | JobExecutionException
                | BeanMisconfiguredException
                | BindFailedException exc) {
            throw new OperationFailedException(exc);
        }
    }

    private static ParametersExperiment createParametersExperiment(
            OutputterChecked outputter, PathPrefixer prefixer, StatefulMessageLogger logger) {
        ExperimentFeedbackContext context =
                new ExperimentFeedbackContext(logger, false, new ExecutionTimeStatistics());
        ParametersExperiment params =
                new ParametersExperiment(
                        new ExecutionArguments(Paths.get(".")),
                        "arbitraryExperimentName",
                        Optional.empty(),
                        outputter,
                        prefixer,
                        context);
        params.setLoggerTaskCreator(createLogReporterBean());
        return params;
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