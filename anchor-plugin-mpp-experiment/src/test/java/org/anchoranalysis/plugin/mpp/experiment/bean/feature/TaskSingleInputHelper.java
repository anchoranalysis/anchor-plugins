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

package org.anchoranalysis.plugin.mpp.experiment.bean.feature;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.bean.xml.RegisterBeanFactories;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.experiment.ExperimentExecutionArguments;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.bean.log.LoggingDestination;
import org.anchoranalysis.experiment.log.reporter.StatefulMessageLogger;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.experiment.task.ParametersUnbound;
import org.anchoranalysis.experiment.task.Task;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.output.bound.BindFailedException;
import org.anchoranalysis.io.output.bound.BoundOutputManager;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.test.image.io.OutputManagerFixture;

/**
 * Executes a task on a single-input outputting into a specific directory
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class TaskSingleInputHelper {

    /**
     * Executes a task on a single-input
     *
     * @param <T> input type
     * @param <S> shared-state type
     * @param <V> task type
     * @param input the input for the task
     * @param task the task to run
     * @param pathDirOutput an absolute path to a directory where outputs of the task will be placed
     * @param pathDirSaved a path (relative to the src/test/resources) to a directory of
     *     saved-results to compare with
     * @param pathsFileToCompare paths (relative to the src/test/resources) to check that are
     *     identical
     * @return true if successful, false otherwise
     * @throws OperationFailedException if anything goes wrong
     */
    public static <T extends InputFromManager, S, V extends Task<T, S>>
            void runTaskAndCompareOutputs(
                    T input,
                    V task,
                    Path pathDirOutput,
                    String pathDirSaved,
                    String[] pathsFileToCompare)
                    throws OperationFailedException {

        boolean successful = runTaskOnSingleInput(input, task, pathDirOutput);
        // Successful outcome
        assertTrue("Sucessful execution of task", successful);

        CompareHelper.compareOutputWithSaved(pathDirOutput, pathDirSaved, pathsFileToCompare);
    }

    /**
     * Executes a task on a single-input
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

            BoundOutputManagerRouteErrors bom =
                    OutputManagerFixture.outputManagerForRouterErrors(pathForOutputs);

            StatefulMessageLogger logger = createStatefulLogReporter();

            ParametersExperiment paramsExp =
                    createParametersExperiment(pathForOutputs, bom.getDelegate(), logger);

            S sharedState = task.beforeAnyJobIsExecuted(bom, paramsExp);

            boolean successful =
                    task.executeJob(new ParametersUnbound<>(paramsExp, input, sharedState, false));

            task.afterAllJobsAreExecuted(sharedState, paramsExp.getContext());

            return successful;
        } catch (AnchorIOException
                | ExperimentExecutionException
                | JobExecutionException
                | BeanMisconfiguredException
                | BindFailedException exc) {
            throw new OperationFailedException(exc);
        }
    }

    private static ParametersExperiment createParametersExperiment(
            Path pathTempFolder, BoundOutputManager outputManager, StatefulMessageLogger logger)
            throws AnchorIOException {
        ParametersExperiment params =
                new ParametersExperiment(
                        new ExperimentExecutionArguments(Paths.get(".")),
                        "arbitraryExperimentName",
                        Optional.empty(),
                        outputManager,
                        logger,
                        false);
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
