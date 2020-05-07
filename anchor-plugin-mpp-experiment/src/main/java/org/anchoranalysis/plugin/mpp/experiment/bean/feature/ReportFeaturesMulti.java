package org.anchoranalysis.plugin.mpp.experiment.bean.feature;

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


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.anchoranalysis.anchor.mpp.bean.init.MPPInitParams;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.define.Define;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.core.text.TypedValue;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.experiment.task.Task;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.io.output.csv.CSVWriter;
import org.anchoranalysis.mpp.io.bean.report.feature.ReportFeatureForSharedObjects;
import org.anchoranalysis.mpp.io.input.MultiInput;
import org.anchoranalysis.mpp.io.input.MPPInitParamsFactory;


public class ReportFeaturesMulti extends Task<MultiInput,CSVWriter> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5961126655531145104L;
	
	// START BEAN PROPERTIES
	@BeanField
	private List<ReportFeatureForSharedObjects> listReportFeatures = new ArrayList<>();
	
	@BeanField @OptionalBean
	private Define define;
	// END BEAN PROPERTIES
	
	@Override
	public CSVWriter beforeAnyJobIsExecuted(
			BoundOutputManagerRouteErrors outputManager, ParametersExperiment params)
			throws ExperimentExecutionException {
				
		CSVWriter writer;
		try {
			writer = CSVWriter.createFromOutputManager("featureReport", outputManager.getDelegate());
		} catch (AnchorIOException e) {
			throw new ExperimentExecutionException(e);
		}
				
		if (writer==null) {
			throw new ExperimentExecutionException("'featureReport' output not enabled, as is required");
		}
		
		List<String> headerNames = ReportFeatureUtilities.genHeaderNames( listReportFeatures, null );
		
		headerNames.add(0, "id" );
		writer.writeHeaders( headerNames );

		return writer;
	}
	
	@Override
	public InputTypesExpected inputTypesExpected() {
		return new InputTypesExpected(MultiInput.class);
	}
	
	@Override
	public void doJobOnInputObject(InputBound<MultiInput,CSVWriter> params)	throws JobExecutionException {
		
		LogErrorReporter logErrorReporter = params.getLogger();
		MultiInput input = params.getInputObject();

		CSVWriter writer = (CSVWriter) params.getSharedState();
		
		if (!writer.isOutputEnabled()) {
			return;
		}
		
		try {
			MPPInitParams soMPP = MPPInitParamsFactory.createFromInput(
				params,
				Optional.ofNullable(define)
			);
			
			List<TypedValue> rowElements = ReportFeatureUtilities.genElementList(
				listReportFeatures,
				soMPP,
				logErrorReporter
			);
			
			rowElements.add(0, new TypedValue(input.descriptiveName(),false) );
			
			writer.writeRow( rowElements );
			
		} catch (CreateException e) {
			throw new JobExecutionException(e);
		}
	}

	@Override
	public void afterAllJobsAreExecuted(
			CSVWriter writer, BoundIOContext context)
			throws ExperimentExecutionException {
		writer.close();
	}
	
	@Override
	public boolean hasVeryQuickPerInputExecution() {
		return false;
	}

	public List<ReportFeatureForSharedObjects> getListReportFeatures() {
		return listReportFeatures;
	}

	public void setListReportFeatures(List<ReportFeatureForSharedObjects> listReportFeatures) {
		this.listReportFeatures = listReportFeatures;
	}

	public Define getDefine() {
		return define;
	}

	public void setDefine(Define define) {
		this.define = define;
	}

}
