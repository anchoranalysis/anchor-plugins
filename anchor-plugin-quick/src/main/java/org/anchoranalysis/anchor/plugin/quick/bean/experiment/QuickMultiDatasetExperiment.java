package org.anchoranalysis.anchor.plugin.quick.bean.experiment;

/*
 * #%L
 * anchor-plugin-mpp-experiment
 * %%
 * Copyright (C) 2016 ETH Zurich, University of Zurich, Owen Feehan
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


import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.StringSet;
import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.experiment.ExperimentExecutionArguments;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.bean.Experiment;
import org.anchoranalysis.experiment.bean.identifier.ExperimentIdentifierConstant;
import org.anchoranalysis.experiment.bean.processor.DebugDependentProcessor;
import org.anchoranalysis.experiment.bean.processor.JobProcessor;
import org.anchoranalysis.experiment.io.IReplaceInputManager;
import org.anchoranalysis.experiment.io.IReplaceOutputManager;
import org.anchoranalysis.experiment.log.ConsoleMessageLogger;
import org.anchoranalysis.experiment.task.Task;
import org.anchoranalysis.experiment.task.processor.MonitoredSequentialExecutor;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.output.bean.OutputManager;

import lombok.Getter;
import lombok.Setter;

// 

/**
 * Makes a lot of assumptions, that allows us to reduce the number of inputs to an InputOutputExperiment
 *
 * Normally, runs an experiment on all data-sets in *datasets*.
 * 
 * However, in debug-mode datasetSpecific is used (if non-empty), otherwise the first dataset is used.
 * 
 * @param <T> InputManagerType
 * @param <S> shared-state
 */
public class QuickMultiDatasetExperiment<T extends InputFromManager, S> extends Experiment implements IReplaceInputManager, IReplaceOutputManager {

	// START BEAN PROPERTIES
	@BeanField @Getter @Setter
	private String folderDataset;
	
	@BeanField @Getter @Setter
	private StringSet datasets;
	
	// Optionally, can specify a specific dataset for debugging. Overrides default behaviour in debug-mode and uses this instead.
	@BeanField @AllowEmpty @Getter @Setter
	private String datasetSpecific = "";
	
	@BeanField @Getter @Setter
	private String beanExtension = ".xml";
	
	/** Relative path to a logger for the experiment in gneeral*/
	@BeanField @Getter @Setter
	private String loggerExperimentPath = "";
	
	/** Relative path to a logger for a specific task */
	@BeanField @Getter @Setter
	private String loggerTaskPath = "";
	
	@BeanField   @Getter @Setter
	private String output;
	
	@BeanField @AllowEmpty @Getter @Setter
	private String identifierSuffix = "";
	
	@BeanField  @Getter @Setter
	private int maxNumProcessors=100;
	
	@BeanField  @Getter @Setter
	private boolean suppressExceptions=true;
	
	@BeanField @Getter @Setter
	private Task<T,S> task;
	// END BEAN PROPERTIES
	
	// Helper for running the experiment repeatedly on different datasets
	private RepeatedExperimentFromXml<T,S> delegate;
	
	private ExperimentIdentifierConstant experimentIdentifier = new ExperimentIdentifierConstant("unnamed", "1.0");
	
	// if non-null, then this InputManager is used, and other settings are ignored
	private InputManager<T> replacementInputManager;
	
	// Possible defaultInstances for beans......... saved from checkMisconfigured for delayed checks elsewhere
	private BeanInstanceMap defaultInstances;
	
	public QuickMultiDatasetExperiment() {
		delegate = new RepeatedExperimentFromXml<>(experimentIdentifier);
	}
		
	@Override
	public void checkMisconfigured(BeanInstanceMap defaultInstances) throws BeanMisconfiguredException {
		super.checkMisconfigured(defaultInstances);
		this.defaultInstances = defaultInstances;
	}

	@Override
	public void localise(Path path) throws BeanMisconfiguredException {
		super.localise(path);
		
		delegate.firstLocalise( getLocalPath(), loggerExperimentPath, loggerTaskPath, output );
	}

	@SuppressWarnings("unchecked")
	@Override
	public void replaceInputManager(InputManager<?> inputManager) throws OperationFailedException {
		this.replacementInputManager = (InputManager<T>) inputManager;
	}
	
	@Override
	public void replaceOutputManager(OutputManager outputManager) throws OperationFailedException {
		delegate.setOutput(outputManager);
	}

	@Override
	public void doExperiment(ExperimentExecutionArguments arguments)
			throws ExperimentExecutionException {
		
		delegate.secondInitBeforeExecution(
			getXMLConfiguration(),
			folderDataset,
			beanExtension,
			createProcessor() 
		);
		
		// If there's a replacement manager run it on this
		if (replacementInputManager!=null) {
			delegate.executeForManager(replacementInputManager, arguments, defaultInstances );
			return;
		}
		
		executeAllDatasets( arguments);
	}
	
	@Override
	public boolean useDetailedLogging() {
		// Always use detailed logging
		return true;
	}
	
	private void executeAllDatasets( ExperimentExecutionArguments expArgs ) {
		Logger reporter = new Logger( new ConsoleMessageLogger() );
		
		MonitoredSequentialExecutor<String> serialExecutor = new MonitoredSequentialExecutor<>(
			name-> executeSingleDataset(name, expArgs, reporter.errorReporter() ),
			name-> name,
			Optional.of(
				reporter.messageLogger()
			),
			true
		);
		
		serialExecutor.executeEachWithMonitor(
			"### Dataset",
			new ArrayList<>(
				selectDatasets(expArgs.isDebugEnabled())
			)	
		);		
	}
	
	private boolean executeSingleDataset( String name, ExperimentExecutionArguments expArgs, ErrorReporter errorReporter ) {
		
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

	/*** Decides which datasets to run the experiment on 
	 * @throws ExperimentExecutionException */
	private Collection<String> selectDatasets(boolean isDebugEnabled) {
		
		// Easy decision when there's no debugging involved
		if (isDebugEnabled) {

			// If there's a specific data-set identified
			if (!datasetSpecific.isEmpty()) {
				return Collections.singleton(datasetSpecific);
			} else {
				return takeFirstIfExists( datasets.set() );	
			}

		} else {
			// Normal behaviour, we do all datasets
			return datasets.set();
		}
	}
	
	/*** Takes the first string from a collection, if the collection is non-empty */
	private static Collection<String> takeFirstIfExists( Collection<String> sets ) {

		if (sets.isEmpty()) {
			// Nothing to do
			return sets;
		}
				
		// Otherwise we take the first dataset, arbitrarily		
		return Collections.singleton(
			sets.iterator().next()
		);
	}
	
	private JobProcessor<T,S> createProcessor() {
		DebugDependentProcessor<T, S> processor = new DebugDependentProcessor<>();
		processor.setMaxNumProcessors(maxNumProcessors);
		processor.setSuppressExceptions(suppressExceptions);
		processor.setTask( task.duplicateBean() );
		return processor;
	}
}
