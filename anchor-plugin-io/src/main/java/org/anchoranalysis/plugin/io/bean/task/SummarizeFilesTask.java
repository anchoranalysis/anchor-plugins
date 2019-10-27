package org.anchoranalysis.plugin.io.bean.task;

/*-
 * #%L
 * anchor-plugin-io
 * %%
 * Copyright (C) 2010 - 2019 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.LogReporter;
import org.anchoranalysis.experiment.ExperimentExecutionArguments;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.task.ParametersBound;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.experiment.task.Task;
import org.anchoranalysis.io.input.FileInput;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.plugin.io.bean.summarizer.Summarizer;
import org.anchoranalysis.plugin.io.bean.summarizer.SummarizerCount;

public class SummarizeFilesTask extends Task<FileInput,Summarizer<Path>> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private Summarizer<Path> pathSummarizer = new SummarizerCount<Path>();
	// END BEAN PROPERTIES
	
	@Override
	public Summarizer<Path> beforeAnyJobIsExecuted(
			BoundOutputManagerRouteErrors outputManager, ParametersExperiment params)
			throws ExperimentExecutionException {

		if (params.isDetailedLogging()) {
			summarizeExperimentArguments(
				params.getLogReporterExperiment(),
				params.getExperimentArguments()
			);
		}
		
		return pathSummarizer;
	}
	
	@Override
	protected void doJobOnInputObject(ParametersBound<FileInput, Summarizer<Path>> params) throws JobExecutionException {
		try {
			params.getSharedState().add( params.getInputObject().pathForBinding() );
		} catch (OperationFailedException e) {
			throw new JobExecutionException(
				String.format("Cannot summarize %s", params.getInputObject().pathForBinding()),
				e
			);
		}
	}
	
	@Override
	public boolean hasVeryQuickPerInputExecution() {
		return true;
	}
	
	private void summarizeExperimentArguments( LogReporter log, ExperimentExecutionArguments eea ) {
		
		if (eea.hasInputDirectory()) {
			log.logFormatted("An input-directory has been set as %s", eea.getInputDirectory() );
		}
		
		if (eea.hasOutputDirectory()) {
			log.logFormatted("An output-directory has been set as %s", eea.getOutputDirectory() );
		}
	}

	@Override
	public void afterAllJobsAreExecuted(BoundOutputManagerRouteErrors outputManager, Summarizer<Path> sharedState,
			LogReporter logReporter) throws ExperimentExecutionException {
		
		try {
			logReporter.log(
				sharedState.describe()
			);
		} catch (OperationFailedException e) {
			throw new ExperimentExecutionException(e);
		}
	}

	public Summarizer<Path> getPathSummarizer() {
		return pathSummarizer;
	}

	public void setPathSummarizer(Summarizer<Path> pathSummarizer) {
		this.pathSummarizer = pathSummarizer;
	}

}
