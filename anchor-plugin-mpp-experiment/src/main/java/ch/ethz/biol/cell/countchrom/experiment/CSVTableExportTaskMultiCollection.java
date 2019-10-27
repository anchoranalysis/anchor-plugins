package ch.ethz.biol.cell.countchrom.experiment;

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


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Optional;
import org.anchoranalysis.bean.define.Define;
import org.anchoranalysis.bean.shared.random.RandomNumberGeneratorBean;
import org.anchoranalysis.bean.shared.random.RandomNumberGeneratorMersenneConstantBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.core.log.LogReporter;
import org.anchoranalysis.core.name.store.SharedObjects;
import org.anchoranalysis.core.text.TypedValue;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.task.ParametersBound;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.experiment.task.Task;
import org.anchoranalysis.image.init.ImageInitParams;
import org.anchoranalysis.io.bean.report.feature.ReportFeature;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.io.output.csv.CSVWriter;
import org.anchoranalysis.io.report.feature.ReportFeatureUtilities;
import org.anchoranalysis.mpp.io.input.MultiInput;

import ch.ethz.biol.cell.beaninitparams.MPPInitParams;


public class CSVTableExportTaskMultiCollection extends Task<MultiInput,CSVWriter> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5961126655531145104L;
	
	// START BEAN PROPERTIES
	@BeanField
	private List<ReportFeature<SharedObjects>> listReportFeatures = new ArrayList<>();
	
	@BeanField @Optional
	private Define namedDefinitions;
	
	@BeanField
	private RandomNumberGeneratorBean randomNumberGenerator = new RandomNumberGeneratorMersenneConstantBean();	
	// END BEAN PROPERTIES
	
	@Override
	public CSVWriter beforeAnyJobIsExecuted(
			BoundOutputManagerRouteErrors outputManager, ParametersExperiment params)
			throws ExperimentExecutionException {
				
		CSVWriter writer;
		try {
			writer = CSVWriter.createFromOutputManager("featureReport", outputManager.getDelegate());
		} catch (IOException e) {
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
	protected void doJobOnInputObject(ParametersBound<MultiInput,CSVWriter> params)	throws JobExecutionException {
		
		LogErrorReporter logErrorReporter = params.getLogErrorReporter();
		MultiInput input = params.getInputObject();

		CSVWriter writer = (CSVWriter) params.getSharedState();
		
		if (!writer.isOutputEnabled()) {
			return;
		}

		
		
		try {
			SharedObjects so = new SharedObjects(logErrorReporter);
			MPPInitParams soMPP = MPPInitParams.create(so, namedDefinitions, logErrorReporter, randomNumberGenerator.create());
			ImageInitParams soImage = soMPP.getImage();
			
			input.addToSharedObjects( soMPP, soImage );
			
			List<TypedValue> rowElements = ReportFeatureUtilities.genElementList(
				listReportFeatures,
				so,
				logErrorReporter
			);
			
			rowElements.add(0, new TypedValue(input.descriptiveName(),false) );
			
			writer.writeRow( rowElements );
			
		} catch (CreateException | OperationFailedException e) {
			throw new JobExecutionException(e);
		}
	}

	@Override
	public void afterAllJobsAreExecuted(
			BoundOutputManagerRouteErrors outputManager, CSVWriter writer, LogReporter logReporter)
			throws ExperimentExecutionException {
		writer.close();
	}
	
	@Override
	public boolean hasVeryQuickPerInputExecution() {
		return false;
	}

	public List<ReportFeature<SharedObjects>> getListReportFeatures() {
		return listReportFeatures;
	}

	public void setListReportFeatures(
			List<ReportFeature<SharedObjects>> listReportFeatures) {
		this.listReportFeatures = listReportFeatures;
	}

	public Define getNamedDefinitions() {
		return namedDefinitions;
	}

	public void setNamedDefinitions(Define namedDefinitions) {
		this.namedDefinitions = namedDefinitions;
	}

	public RandomNumberGeneratorBean getRandomNumberGenerator() {
		return randomNumberGenerator;
	}

	public void setRandomNumberGenerator(RandomNumberGeneratorBean randomNumberGenerator) {
		this.randomNumberGenerator = randomNumberGenerator;
	}
}
