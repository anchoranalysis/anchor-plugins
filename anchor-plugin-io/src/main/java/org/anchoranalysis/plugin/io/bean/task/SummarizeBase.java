/*-
 * #%L
 * anchor-plugin-io
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

package org.anchoranalysis.plugin.io.bean.task;

import java.nio.file.Path;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.concurrency.ConcurrencyPlan;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.log.MessageLogger;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.arguments.ExecutionArguments;
import org.anchoranalysis.experiment.bean.task.Task;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.plugin.io.bean.summarizer.Summarizer;
import org.anchoranalysis.plugin.io.bean.summarizer.SummarizerCount;

public abstract class SummarizeBase<T extends InputFromManager, S> extends Task<T, Summarizer<S>> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private Summarizer<S> summarizer = new SummarizerCount<>();
    // END BEAN PROPERTIES

    @Override
    public Summarizer<S> beforeAnyJobIsExecuted(
            Outputter outputter,
            ConcurrencyPlan concurrencyPlan,
            List<T> inputs,
            ParametersExperiment params)
            throws ExperimentExecutionException {

        if (params.isDetailedLogging()) {
            summarizeExperimentArguments(
                    params.getLoggerExperiment(), params.getExperimentArguments());
        }

        return summarizer;
    }

    @Override
    public void doJobOnInput(InputBound<T, Summarizer<S>> params) throws JobExecutionException {
        try {
            params.getSharedState().add(extractObjectForSummary(params.getInput()));
        } catch (OperationFailedException e) {
            
            String message = String.format("Cannot summarize %s",
                    params.getInput().pathForBinding().map(Path::toString).orElse("the input."));
            
            throw new JobExecutionException(message, e);
        }
    }

    @Override
    public void afterAllJobsAreExecuted(Summarizer<S> sharedState, InputOutputContext context)
            throws ExperimentExecutionException {

        try {
            context.getMessageReporter().log(sharedState.describe());
        } catch (OperationFailedException e) {
            throw new ExperimentExecutionException(e);
        }
    }

    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return true;
    }

    /** Extract object for summary. */
    protected abstract S extractObjectForSummary(T input);

    private void summarizeExperimentArguments(MessageLogger log, ExecutionArguments arguments) {
        arguments
                .input()
                .getInputDirectory()
                .ifPresent(dir -> log.logFormatted("An input-directory has been set as %s", dir));

        arguments
                .output()
                .getPrefixer()
                .getOutputDirectory()
                .ifPresent(dir -> log.logFormatted("An output-directory has been set as %s", dir));
    }
}
