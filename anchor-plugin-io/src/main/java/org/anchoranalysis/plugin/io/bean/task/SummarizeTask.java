/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.task;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.MessageLogger;
import org.anchoranalysis.experiment.ExperimentExecutionArguments;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.experiment.task.Task;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.plugin.io.bean.summarizer.Summarizer;
import org.anchoranalysis.plugin.io.bean.summarizer.SummarizerCount;

public abstract class SummarizeTask<T extends InputFromManager, S> extends Task<T, Summarizer<S>> {

    // START BEAN PROPERTIES
    @BeanField private Summarizer<S> summarizer = new SummarizerCount<>();
    // END BEAN PROPERTIES

    @Override
    public Summarizer<S> beforeAnyJobIsExecuted(
            BoundOutputManagerRouteErrors outputManager, ParametersExperiment params)
            throws ExperimentExecutionException {

        if (params.isDetailedLogging()) {
            summarizeExperimentArguments(
                    params.getLoggerExperiment(), params.getExperimentArguments());
        }

        return summarizer;
    }

    @Override
    public void doJobOnInputObject(InputBound<T, Summarizer<S>> params)
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

    public Summarizer<S> getSummarizer() {
        return summarizer;
    }

    public void setSummarizer(Summarizer<S> summarizer) {
        this.summarizer = summarizer;
    }
}
