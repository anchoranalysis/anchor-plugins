package ch.ethz.biol.cell.countchrom.experiment;

import java.nio.file.Path;

import org.anchoranalysis.bean.annotation.AllowEmpty;

/*
 * #%L
 * anchor-plugin-image-task
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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.log.LogReporter;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.task.ParametersBound;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.experiment.task.Task;
import org.anchoranalysis.io.bean.root.RootPathMap;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.generator.text.StringGenerator;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;

public class RecordFilepathsTask<T extends InputFromManager> extends Task<T,StringBuilder> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	
	// The name of the RootPath to associate with this fileset. If empty, it is ignored.
	@BeanField @AllowEmpty
	private String rootName = "";
	// END BEAN PROPERTIES

	@Override
	public StringBuilder beforeAnyJobIsExecuted(
			BoundOutputManagerRouteErrors outputManager, ParametersExperiment params)
			throws ExperimentExecutionException {
		return new StringBuilder();
	}

	@Override
	protected void doJobOnInputObject(ParametersBound<T,StringBuilder> params)
			throws JobExecutionException {

		Path path = params.getInputObject().pathForBinding();
		
		StringBuilder sb = params.getSharedState();
		
		if (!rootName.isEmpty()) {
			try {
				boolean debugMode = params.getExperimentArguments().isDebugEnabled();
				path = RootPathMap.instance().split(path, rootName, debugMode).getPath();
			} catch (AnchorIOException e) {
				throw new JobExecutionException(e);
			}
		}
		sb.append("<item>");
		sb.append(path);
		sb.append("</item>");
		sb.append('\n');
	}

	@Override
	public void afterAllJobsAreExecuted(
			BoundOutputManagerRouteErrors outputManager, StringBuilder sharedState, LogReporter logReporter)
			throws ExperimentExecutionException {
		
		outputManager.getWriterAlwaysAllowed().write(
			"list",
			() -> new StringGenerator(
				sharedState.toString()
			)
		);
	}

	public String getRootName() {
		return rootName;
	}

	public void setRootName(String rootName) {
		this.rootName = rootName;
	}

	@Override
	public boolean hasVeryQuickPerInputExecution() {
		return true;
	}
}
