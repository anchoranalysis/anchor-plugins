package org.anchoranalysis.plugin.io.bean.task;

/*
 * #%L
 * anchor-plugin-io
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


import java.io.IOException;
import java.nio.file.Path;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.log.LogReporter;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.task.ParametersBound;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.experiment.task.Task;
import org.anchoranalysis.io.csv.reader.CSVReaderByLine;
import org.anchoranalysis.io.csv.reader.CSVReaderByLine.ReadByLine;
import org.anchoranalysis.io.input.FileInput;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.io.output.csv.CSVWriter;

// At the moment, we don't check if the name number of rows/columns exist
public class CombineCSVTask extends Task<FileInput,CSVWriter> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5961126655531145104L;
	
	// START BEAN PROPERTIES
	@BeanField
	private String seperator = ",";
	
	@BeanField
	private boolean firstLineHeaders = true;
	
	@BeanField
	private boolean transposed = false;
	
	@BeanField
	private boolean addName = true;
	// END BEAN PROPERTIES
	
	@Override
	public CSVWriter beforeAnyJobIsExecuted( BoundOutputManagerRouteErrors outputManager, ParametersExperiment params)	throws ExperimentExecutionException {
	
		try {
			CSVWriter writer = CSVWriter.createFromOutputManager(
				"featureReport",
				outputManager.getDelegate()
			);
			
			if (writer==null) {
				throw new ExperimentExecutionException("'featureReport' output not enabled, as is required");
			}
				
			return writer;
			
		} catch (IOException e) {
			throw new ExperimentExecutionException(e);
		}
	}
	
	@Override
	public boolean hasVeryQuickPerInputExecution() {
		return false;
	}
	
	
	@Override
	protected void doJobOnInputObject(ParametersBound<FileInput,CSVWriter> params) throws JobExecutionException {
		
		FileInput inputObject = params.getInputObject();
		
		
		CSVWriter writer = (CSVWriter) params.getSharedState();
		
		if (writer==null || !writer.isOutputEnabled()) {
			return;
		}
		
		Path inputPath = inputObject.getFile().toPath();
		try (ReadByLine readByLine = CSVReaderByLine.open(inputPath,seperator,firstLineHeaders)) {
			
			String name = addName?inputObject.descriptiveName():null;	// NULL means no-name is added
			AddWithName addWithName = new AddWithName(writer,firstLineHeaders,name);
			
			if (transposed) {
				addWithName.addTransposed( readByLine );
			} else {
				addWithName.addNonTransposed( readByLine );
			}
		
		} catch (IOException e) {
			throw new JobExecutionException(e);
		}
	}

	@Override
	public void afterAllJobsAreExecuted( BoundOutputManagerRouteErrors outputManager, CSVWriter writer, LogReporter logReporter )
			throws ExperimentExecutionException {
		
		if (writer!=null) {
			writer.close();
		}
	}

	public String getSeperator() {
		return seperator;
	}

	public void setSeperator(String seperator) {
		this.seperator = seperator;
	}

	public boolean isFirstLineHeaders() {
		return firstLineHeaders;
	}

	public void setFirstLineHeaders(boolean firstLineHeaders) {
		this.firstLineHeaders = firstLineHeaders;
	}

	public boolean isTransposed() {
		return transposed;
	}

	public void setTransposed(boolean transposed) {
		this.transposed = transposed;
	}

	public boolean isAddName() {
		return addName;
	}

	public void setAddName(boolean addName) {
		this.addName = addName;
	}

}
