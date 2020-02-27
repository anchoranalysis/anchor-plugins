package org.anchoranalysis.plugin.image.task.bean.feature;

/*-
 * #%L
 * anchor-plugin-image-task
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
import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.NonEmpty;
import org.anchoranalysis.bean.error.BeanDuplicateException;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.core.name.MultiName;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.task.ParametersBound;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.ResultsVector;
import org.anchoranalysis.feature.io.csv.GroupedResultsVectorCollection;
import org.anchoranalysis.feature.list.NamedFeatureStore;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.plugin.image.task.sharedstate.SharedStateExportFeaturesWithStore;

/**
 * Base class for exporting features, where features are calculated per-image
 *   using a NamedFeatureStore
 *   
 * @author FEEHANO
 *
 */
public abstract class ExportFeaturesStoreTask<T extends InputFromManager> extends ExportFeaturesTask<T,SharedStateExportFeaturesWithStore> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField @NonEmpty
	private List<NamedBean<FeatureListProvider>> listFeatures = new ArrayList<NamedBean<FeatureListProvider>>();
	// END BEAN PROPERTIES
	
	private String firstResultHeader;

	/**
	 * Default constructor
	 * 
	 * @param firstResultHeader the first column-name in the CSV file that is outputted
	 */
	public ExportFeaturesStoreTask(String firstResultHeader) {
		super();
		this.firstResultHeader = firstResultHeader;
	}	

	@Override
	public SharedStateExportFeaturesWithStore beforeAnyJobIsExecuted(BoundOutputManagerRouteErrors outputManager, ParametersExperiment params)
			throws ExperimentExecutionException {
		try {
			return new SharedStateExportFeaturesWithStore(
				getListFeatures(),
				new GroupedResultsVectorCollection(firstResultHeader,"group")
			);
		} catch (CreateException e) {
			throw new ExperimentExecutionException(e);
		}
	}

	@Override
	protected void doJobOnInputObject( ParametersBound<T,SharedStateExportFeaturesWithStore> params ) throws JobExecutionException {
		
		try {
			ResultsVector rv = calcResultsVectorForInputObject(
				params.getInputObject(),
				params.getSharedState().getFeatureStore(),
				params.getOutputManager(),
				params.getLogErrorReporter()
			);
			storeResults(params, rv);
			
		} catch (OperationFailedException | BeanDuplicateException | FeatureCalcException e) {
			throw new JobExecutionException(e);
		}
	}
	
	protected abstract ResultsVector calcResultsVectorForInputObject(
		T inputObject,
		NamedFeatureStore featureStore,
		BoundOutputManagerRouteErrors outputManager,
		LogErrorReporter logErrorReporter
	) throws FeatureCalcException;


	private void storeResults(ParametersBound<T,SharedStateExportFeaturesWithStore> params, ResultsVector rv) throws OperationFailedException {
		
		MultiName identifier = identifierFor( params.getInputObject() );
		
		try {
			params.getSharedState().resultsVectorForIdentifier(identifier).add( rv );
		} catch ( GetOperationFailedException e ) {
			throw new OperationFailedException(e);
		}
	}
	
	private MultiName identifierFor( T inputObject ) throws OperationFailedException {
		
		try {
			Path inputPath = inputObject.pathForBinding();
			return new GroupAndImageNames(
				extractGroupName(inputPath, false),
				extractImageIdentifier(inputPath, false)
			);		
		} catch (AnchorIOException e) {
			throw new OperationFailedException(e);
		}
	}
	
	public List<NamedBean<FeatureListProvider>> getListFeatures() {
		return listFeatures;
	}

	public void setListFeatures(
			List<NamedBean<FeatureListProvider>> listFeatures) {
		this.listFeatures = listFeatures;
	}
}
