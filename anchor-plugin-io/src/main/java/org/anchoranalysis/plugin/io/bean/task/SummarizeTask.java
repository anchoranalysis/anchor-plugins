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

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.concurrency.ConcurrencyPlan;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.MessageLogger;
import org.anchoranalysis.experiment.ExperimentExecutionArguments;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.bean.task.Task;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.plugin.io.bean.summarizer.Summarizer;
import org.anchoranalysis.plugin.io.bean.summarizer.SummarizerCount;

public abstract class SummarizeTask<T extends InputFromManager, S> extends Task<T, Summarizer<S>> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private Summarizer<S> summarizer = new SummarizerCount<>();
    // END BEAN PROPERTIES

    @Override
    public Summarizer<S> beforeAnyJobIsExecuted(
            BoundOutputManagerRouteErrors outputManager,
            ConcurrencyPlan concurrencyPlan,
            ParametersExperiment params)
            throws ExperimentExecutionException {

        if (params.isDetailedLogging()) {
            summarizeExperimentArguments(
                    params.getLoggerExperiment(), params.getExperimentArguments());
        }

        return summarizer;
    }

    @Override
    public void doJobOnInput(InputBound<T, Summarizer<S>> params)
            throws JobExecutionException {
        try {
            params.getSharedState().add(extractObjectForSummary(params.getInputObject()));
        } catch (OperationFailedException e) {
            throw new JobExecutionException(
                    String.format("Cannot summarize %s", params.getInputObject().pathForBinding()),
                    e);
        }
    }

    @Override
    public void afterAllJobsAreExecuted(Summarizer<S> sharedState, BoundIOContext context)
            throws ExperimentExecutionException {

        try {
            context.getLogReporter().log(sharedState.describe());
        } catch (OperationFailedException e) {
            throw new ExperimentExecutionException(e);
        }
    }

    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return true;
    }

    // Extract object for summary
    protected abstract S extractObjectForSummary(T input);

    private void summarizeExperimentArguments(MessageLogger log, ExperimentExecutionArguments eea) {

        eea.getInputDirectory()
                .ifPresent(dir -> log.logFormatted("An input-directory has been set as %s", dir));

        eea.getOutputDirectory()
                .ifPresent(dir -> log.logFormatted("An output-directory has been set as %s", dir));
    }
}
