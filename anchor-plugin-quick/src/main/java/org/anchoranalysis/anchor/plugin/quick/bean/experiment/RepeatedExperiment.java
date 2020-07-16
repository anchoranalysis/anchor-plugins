/* (C)2020 */
package org.anchoranalysis.anchor.plugin.quick.bean.experiment;

import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.experiment.ExperimentExecutionArguments;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.bean.identifier.ExperimentIdentifier;
import org.anchoranalysis.experiment.bean.io.InputOutputExperiment;
import org.anchoranalysis.experiment.bean.log.LoggingDestination;
import org.anchoranalysis.experiment.bean.processor.JobProcessor;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.output.bean.OutputManager;
import org.apache.commons.configuration.XMLConfiguration;

/***
 * Helper framework for running an experiment repeatedly on different input-managers
 *
 */
class RepeatedExperiment<T extends InputFromManager, S> {

    private InputOutputExperiment<T, S> delegate;
    private JobProcessor<T, S> taskProcessor;

    public RepeatedExperiment(ExperimentIdentifier experimentIdentifier) {
        delegate = new InputOutputExperiment<>();
        delegate.setExperimentIdentifier(experimentIdentifier);
    }

    // Should be called after the constructor, before any other methods
    public void init(XMLConfiguration xmlConfiguration, JobProcessor<T, S> taskProcessor) {
        delegate.associateXml(xmlConfiguration);
        this.taskProcessor = taskProcessor;
    }

    /**
     * Runs the experiment for a particular input-manager
     *
     * @param inputManager
     * @param expArgs
     * @param defaultInstances
     * @param context
     * @throws ExperimentExecutionException
     */
    public void executeForManager(
            InputManager<T> inputManager,
            ExperimentExecutionArguments expArgs,
            BeanInstanceMap defaultInstances)
            throws ExperimentExecutionException {

        delegate.setInput(inputManager);
        delegate.setTaskProcessor(taskProcessor);
        checkConfiguration(defaultInstances);

        delegate.doExperiment(expArgs);
    }

    private void checkConfiguration(BeanInstanceMap defaultInstances)
            throws ExperimentExecutionException {

        try {
            delegate.checkMisconfigured(defaultInstances);
        } catch (BeanMisconfiguredException e) {
            throw new ExperimentExecutionException("The bean is misconfigured", e);
        }
    }

    public void setLogExperiment(LoggingDestination logger) {
        delegate.setLogExperiment(logger);
    }

    public void setLogTask(LoggingDestination logger) {
        delegate.setLogTask(logger);
    }

    public void setOutput(OutputManager output) {
        delegate.setOutput(output);
    }
}
