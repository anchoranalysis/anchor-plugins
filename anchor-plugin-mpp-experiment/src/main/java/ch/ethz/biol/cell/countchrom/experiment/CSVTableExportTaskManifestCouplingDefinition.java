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


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.core.text.TypedValue;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.bean.task.TaskWithoutSharedState;
import org.anchoranalysis.experiment.task.ParametersBound;
import org.anchoranalysis.io.bean.report.feature.ReportFeature;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.manifest.ManifestRecorderFile;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.io.output.csv.CSVWriter;
import org.anchoranalysis.io.report.feature.ReportFeatureUtilities;
import org.anchoranalysis.plugin.io.manifest.CoupledManifests;
import org.anchoranalysis.plugin.io.manifest.ManifestCouplingDefinition;


public class CSVTableExportTaskManifestCouplingDefinition extends TaskWithoutSharedState<ManifestCouplingDefinition> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5961126655531145104L;
	
	// START BEAN PROPERTIES
	@BeanField
	private List<ReportFeature<ManifestRecorderFile>> listReportFeatures = new ArrayList<>();
	// END BEAN PROPERTIES

	@Override
	protected void doJobOnInputObject(ParametersBound<ManifestCouplingDefinition,Object> params ) throws JobExecutionException {
		
		LogErrorReporter logErrorReporter = params.getLogErrorReporter();
		ManifestCouplingDefinition input = params.getInputObject();
		BoundOutputManagerRouteErrors outputManager = params.getOutputManager();
		
		CSVWriter writer;
		try {
			writer = CSVWriter.createFromOutputManager("featureReport", outputManager.getDelegate());
		} catch (AnchorIOException e1) {
			throw new JobExecutionException(e1);
		}
		
		try {
					
			if (writer==null) {
				return;
			}
			
			List<String> headerNames = ReportFeatureUtilities.genHeaderNames( listReportFeatures, logErrorReporter );
			
			writer.writeHeaders( headerNames );

			
			Iterator<CoupledManifests> itr = input.iteratorCoupledManifests();
			while ( itr.hasNext() ) {
				
				CoupledManifests mr = itr.next();

				List<TypedValue> rowElements = ReportFeatureUtilities.genElementList(
					listReportFeatures,
					mr.getFileManifest(),
					logErrorReporter
				); 

				try {
					writer.writeRow( rowElements );
				} catch (NumberFormatException e) {
					throw new JobExecutionException(e);
				}
			}
			
		} finally {
			writer.close();
		}
		
	}
	
	@Override
	public boolean hasVeryQuickPerInputExecution() {
		return false;
	}

	public List<ReportFeature<ManifestRecorderFile>> getListReportFeatures() {
		return listReportFeatures;
	}

	public void setListReportFeatures(
			List<ReportFeature<ManifestRecorderFile>> listReportFeatures) {
		this.listReportFeatures = listReportFeatures;
	}
}
