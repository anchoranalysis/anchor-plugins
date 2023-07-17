/*-
 * #%L
 * anchor-plugin-quick
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

package org.anchoranalysis.plugin.quick.bean.experiment;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.exception.BeanMisconfiguredException;
import org.anchoranalysis.bean.primitive.StringSet;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.format.NonImageFileFormat;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.log.error.ErrorReporter;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.arguments.ExecutionArguments;
import org.anchoranalysis.experiment.bean.Experiment;
import org.anchoranalysis.experiment.bean.identifier.ExperimentIdentifierConstant;
import org.anchoranalysis.experiment.bean.io.InputOutputExperiment;
import org.anchoranalysis.experiment.bean.processor.DebugDependentProcessor;
import org.anchoranalysis.experiment.bean.processor.JobProcessor;
import org.anchoranalysis.experiment.bean.task.Task;
import org.anchoranalysis.experiment.io.ReplaceInputManager;
import org.anchoranalysis.experiment.io.ReplaceOutputManager;
import org.anchoranalysis.experiment.log.ConsoleMessageLogger;
import org.anchoranalysis.experiment.task.processor.MonitoredSequentialExecutor;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.input.bean.InputManager;
import org.anchoranalysis.io.output.bean.OutputManager;

/**
 * Makes a lot of assumptions, that allows us to reduce the number of inputs to an {@link
 * InputOutputExperiment}.
 *
 * <p>Normally, runs an experiment on all data-sets in *datasets*.
 *
 * <p>However, in debug-mode {@code datasetSpecific} is used (if non-empty), otherwise the first
 * dataset is used.
 *
 * @param <T> input-object type
 * @param <S> shared-state
 */
public class QuickMultiDatasetExperiment<T extends InputFromManager, S> extends Experiment
        implements ReplaceInputManager<T>, ReplaceOutputManager {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private String directoryDataset;

    @BeanField @Getter @Setter private StringSet datasets;

    // Optionally, can specify a specific dataset for debugging. Overrides default behaviour in
    // debug-mode and uses this instead.
    @BeanField @AllowEmpty @Getter @Setter private String datasetSpecific = "";

    @BeanField @Getter @Setter
    private String beanExtension = NonImageFileFormat.XML.extensionWithPeriod();

    /** Relative path to a logger for the experiment in gneeral */
    @BeanField @Getter @Setter private String logExperimentPath = "";

    /** Relative path to a logger for a specific task */
    @BeanField @Getter @Setter private String logTaskPath = "";

    @BeanField @Getter @Setter private String output;

    @BeanField @AllowEmpty @Getter @Setter private String identifierSuffix = "";

    /**
     * An upper limit on the number of the processors that can be simultaneously used in parallel,
     * if they are available.
     */
    @BeanField @Getter @Setter private int maxNumberProcessors = 100;

    @BeanField @Getter @Setter private boolean suppressExceptions = true;

    @BeanField @Getter @Setter private Task<T, S> task;
    // END BEAN PROPERTIES

    // Helper for running the experiment repeatedly on different datasets
    private RepeatedExperimentFromXml<T, S> delegate;

    private ExperimentIdentifierConstant experimentIdentifier =
            new ExperimentIdentifierConstant("unnamed", "1.0");

    // if non-null, then this InputManager is used, and other settings are ignored
    private InputManager<T> replacementInputManager;

    // Possible defaultInstances for beans......... saved from checkMisconfigured for delayed checks
    // elsewhere
    private BeanInstanceMap defaultInstances;

    public QuickMultiDatasetExperiment() {
        delegate = new RepeatedExperimentFromXml<>(experimentIdentifier);
    }

    @Override
    public void checkMisconfigured(BeanInstanceMap defaultInstances)
            throws BeanMisconfiguredException {
        super.checkMisconfigured(defaultInstances);
        this.defaultInstances = defaultInstances;
    }

    @Override
    public void localise(Path path) throws BeanMisconfiguredException {
        super.localise(path);
        Optional<Path> localPath = getLocalPath();
        if (localPath.isPresent()) {
            delegate.firstLocalise(localPath.get(), logExperimentPath, logTaskPath, output);
        } else {
            throw new BeanMisconfiguredException("No local path is associated with the bean");
        }
    }

    @Override
    public void replaceInputManager(InputManager<T> inputManager) throws OperationFailedException {
        this.replacementInputManager = inputManager;
    }

    @Override
    public void replaceOutputManager(OutputManager output) throws OperationFailedException {
        delegate.setOutput(output);
    }

    @Override
    public Optional<Path> executeExperiment(ExecutionArguments arguments)
            throws ExperimentExecutionException {

        delegate.secondInitBeforeExecution(
                getXMLConfiguration(), directoryDataset, beanExtension, createProcessor());

        // If there's a replacement manager run it on this
        if (replacementInputManager != null) {
            return delegate.executeForManager(replacementInputManager, arguments, defaultInstances);
        }

        executeAllDatasets(arguments);

        // No single output path exists, as multiple datasets were executed.
        return Optional.empty();
    }

    @Override
    public boolean useDetailedLogging() {
        // Always use detailed logging
        return true;
    }

    private void executeAllDatasets(ExecutionArguments expArgs) {
        Logger reporter = new Logger(new ConsoleMessageLogger());

        MonitoredSequentialExecutor<String> serialExecutor =
                new MonitoredSequentialExecutor<>(
                        name -> executeSingleDataset(name, expArgs, reporter.errorReporter()),
                        name -> name,
                        Optional.of(reporter.messageLogger()),
                        true);

        serialExecutor.executeEachWithMonitor(
                "### Dataset", new ArrayList<>(selectDatasets(expArgs.isDebugModeEnabled())));
    }

    private boolean executeSingleDataset(
            String name, ExecutionArguments expArgs, ErrorReporter errorReporter) {

        // Set the name of the experiment
        experimentIdentifier.setName(name + identifierSuffix);

        try {
            delegate.executeForManagerFromXml(name, expArgs, defaultInstances);

            // The dataset has successfully executed if there is no Exception
            return true;

        } catch (ExperimentExecutionException e) {
            errorReporter.recordError(QuickMultiDatasetExperiment.class, e);

            return false;
        }
    }

    /***
     * Decides which datasets to run the experiment on
     */
    private Collection<String> selectDatasets(boolean isDebugEnabled) {

        // Easy decision when there's no debugging involved
        if (isDebugEnabled) {

            // If there's a specific data-set identified
            if (!datasetSpecific.isEmpty()) {
                return Collections.singleton(datasetSpecific);
            } else {
                return takeFirstIfExists(datasets.set());
            }

        } else {
            // Normal behaviour, we do all datasets
            return datasets.set();
        }
    }

    /*** Takes the first string from a collection, if the collection is non-empty */
    private static Collection<String> takeFirstIfExists(Collection<String> sets) {

        if (sets.isEmpty()) {
            // Nothing to do
            return sets;
        }

        // Otherwise we take the first dataset, arbitrarily
        return Collections.singleton(sets.iterator().next());
    }

    private JobProcessor<T, S> createProcessor() {
        DebugDependentProcessor<T, S> processor = new DebugDependentProcessor<>();
        processor.setMaxNumberProcessors(maxNumberProcessors);
        processor.setSuppressExceptions(suppressExceptions);
        processor.setTask(task.duplicateBean());
        return processor;
    }
}
