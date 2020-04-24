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


import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.anchor.mpp.bean.init.GeneralInitParams;
import org.anchoranalysis.anchor.mpp.bean.init.MPPInitParams;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.define.Define;
import org.anchoranalysis.bean.error.BeanDuplicateException;
import org.anchoranalysis.bean.shared.params.keyvalue.KeyValueParamsProvider;
import org.anchoranalysis.bean.shared.random.RandomNumberGeneratorBean;
import org.anchoranalysis.bean.shared.random.RandomNumberGeneratorMersenneConstantBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.core.name.CombinedName;
import org.anchoranalysis.core.name.MultiName;
import org.anchoranalysis.core.name.store.SharedObjects;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.ParametersBound;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.calc.results.ResultsVectorCollection;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.io.csv.GroupedResultsVectorCollection;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.init.ImageInitParams;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.mpp.io.input.MultiInput;
import org.anchoranalysis.mpp.io.output.NRGStackWriter;
import org.anchoranalysis.plugin.image.task.bean.feature.ExportFeaturesTask;
import org.anchoranalysis.plugin.image.task.sharedstate.SharedStateExportFeatures;
import org.anchoranalysis.plugin.mpp.experiment.bean.feature.flexi.FlexiFeatureTable;
import org.anchoranalysis.plugin.mpp.experiment.feature.FeatureSessionFlexiFeatureTable;
import org.anchoranalysis.plugin.mpp.experiment.outputter.SharedObjectsUtilities;


/** Calculates feature on a 'grouped' set of objects
 
   1. All files are aggregated into groups (with the name of the ObjMaskProvider added to the end)
   2. For each image file, the NamedDefinitions are applied and an ObjMaskCollection is extracted
   3. These objects are added to the appropriate ObjMaskCollection associated with each group
   4. ReportFeatures are calculated on each group, and exported as a properties file
   
   csvAll  		one csv file where each row is an object
   csvAgg  		one csv file where each row is a group (with aggregated features of the objects within)
   csvGroup 	a csv file per group, where each row is an object
   
   @param T the feature input-type supported by the FlexiFeatureTable
   
**/
public class ExportFeaturesObjMaskTask<T extends FeatureInput> extends ExportFeaturesTask<MultiInput,SharedStateExportFeaturesObjMask<T>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5961126655531145104L;
	
	// START BEAN PROPERTIES
	@BeanField
	private List<NamedBean<FeatureListProvider<FeatureInputSingleObj>>> listFeaturesObjMask = new ArrayList<>();
	
	@BeanField @OptionalBean
	private Define namedDefinitions;
	
	@BeanField
	private List<NamedBean<ObjMaskProvider>> listObjMaskProvider = new ArrayList<>();
	
	@BeanField
	private StackProvider nrgStackProvider;
	
	@BeanField @OptionalBean
	private KeyValueParamsProvider nrgParamsProvider;
	
	@BeanField
	private RandomNumberGeneratorBean randomNumberGenerator = new RandomNumberGeneratorMersenneConstantBean();
	
	@BeanField
	private FlexiFeatureTable<T> selectFeaturesObjects;
	
	/**
	 * If non-empty, A keyValueParams is treated as part of the nrgStack 
	 */
	@BeanField @AllowEmpty
	private String nrgParamsName = "";
	// END BEAN PROPERTIES
	
	@Override
	public SharedStateExportFeaturesObjMask<T> beforeAnyJobIsExecuted(
			BoundOutputManagerRouteErrors outputManager,
			ParametersExperiment params
	) throws ExperimentExecutionException {
		
		try {
			return new SharedStateExportFeaturesObjMask<>(
				new GroupedResultsVectorCollection("id","group","objSetName"),
				selectFeaturesObjects.createFeatures(listFeaturesObjMask)
			);
			
		} catch (CreateException | InitException e) {
			throw new ExperimentExecutionException(e);
		}
	}
	
	private NRGStackWithParams createNRGStack( ImageInitParams so, LogErrorReporter logger ) throws InitException, CreateException {

		// Extract the NRG stack
		StackProvider nrgStackProviderLoc = nrgStackProvider.duplicateBean();
		nrgStackProviderLoc.initRecursive(so, logger);
		NRGStackWithParams stack = new NRGStackWithParams(nrgStackProviderLoc.create());

		if(nrgParamsProvider!=null) {
			nrgParamsProvider.initRecursive(so.getParams(), logger);
			stack.setParams(nrgParamsProvider.create());
		}
		return stack;
	}
	
	@Override
	public InputTypesExpected inputTypesExpected() {
		return new InputTypesExpected(MultiInput.class);
	}

	@Override
	public void doJobOnInputObject(	ParametersBound<MultiInput,SharedStateExportFeaturesObjMask<T>> params	) throws JobExecutionException {
		
		LogErrorReporter logErrorReporter = params.getLogErrorReporter();
		MultiInput inputObject = params.getInputObject();
		
		try {
			// Create a duplicated featureStore for this image, as we want separate features on this thread,
			//  so they are thread-safe, for parallel execution
			FeatureSessionFlexiFeatureTable<T> session = params.getSharedState().getSession().duplicateForNewThread();
						
			MPPInitParams soMPP = createInitParams(
				inputObject,
				ParamsHelper.createGeneralParams(randomNumberGenerator.create(), params)
			);
			
			NRGStackWithParams nrgStack = createNRGStack(soMPP.getImage(), logErrorReporter );
			
			session.start( soMPP.getImage(), soMPP.getFeature(), nrgStack, logErrorReporter );
			
			outputSharedObjs( soMPP, nrgStack, params.getOutputManager(), logErrorReporter );
			
			processAllProviders(
				params.getExperimentArguments().isDebugEnabled(),
				inputObject.pathForBinding(),
				session,
				soMPP.getImage(),
				nrgStack,
				params.getSharedState(),
				logErrorReporter
			);
						
		} catch (OperationFailedException | CreateException | InitException | BeanDuplicateException e) {
			throw new JobExecutionException(e);
		}
	}
	
	private MPPInitParams createInitParams( MultiInput inputObject, GeneralInitParams paramsGeneral ) throws CreateException, OperationFailedException {
		SharedObjects so = new SharedObjects( paramsGeneral.getLogErrorReporter()	);
		MPPInitParams soMPP = MPPInitParams.create(
			so,
			namedDefinitions,
			paramsGeneral
		);
		inputObject.addToSharedObjects( soMPP, soMPP.getImage() );
		return soMPP;
	}
	
	// General objects can be outputted
	private void outputSharedObjs(
		MPPInitParams soMPP,
		NRGStackWithParams nrgStack,
		BoundOutputManagerRouteErrors outputManager,
		LogErrorReporter logErrorReporter
	) {
		SharedObjectsUtilities.output(soMPP, outputManager, logErrorReporter, false);
		
		NRGStackWriter.writeNRGStack(
			nrgStack,
			outputManager,
			logErrorReporter
		);
	}
	
	private void processAllProviders(
		boolean debugMode,
		Path inputPath,
		FeatureSessionFlexiFeatureTable<T> session,
		ImageInitParams imageInitParams,
		NRGStackWithParams nrgStack,
		SharedStateExportFeatures sharedState,
		LogErrorReporter logErrorReporter		
	) throws OperationFailedException {
		try {
			String id = extractImageIdentifier(inputPath, debugMode);
			
			// Extract a group name
			String groupName = extractGroupName(inputPath, debugMode);
									
			// For every objMaskCollection provider
			for( NamedBean<ObjMaskProvider> ni : listObjMaskProvider) {
				MultiName rowName = new CombinedName( groupName, ni.getName() ); 
				calculateFeaturesForProvider(
					id,
					objsFromProvider(ni.getValue(), imageInitParams, logErrorReporter),
					session,
					nrgStack,
					sharedState.resultsVectorForIdentifier(rowName),
					logErrorReporter
				);
			}
		} catch (AnchorIOException | GetOperationFailedException e) {
			throw new OperationFailedException(e);
		}
	}
	
	
	private void calculateFeaturesForProvider(
		String id,
		ObjMaskCollection objs,
		FeatureSessionFlexiFeatureTable<T> session,
		NRGStackWithParams nrgStack,
		ResultsVectorCollection resultsDestination,
		LogErrorReporter logErrorReporter
	) throws OperationFailedException {
		try {
			List<T> listParams = selectFeaturesObjects.createListCalcParams(
				objs,
				nrgStack,
				logErrorReporter
			);
			
			FeatureCalculator.calculateManyFeaturesInto(
				id,
				session,
				listParams,
				resultsDestination,
				logErrorReporter
			);
		} catch (CreateException | OperationFailedException e) {
			throw new OperationFailedException(e);
		}
	}
	
	private static ObjMaskCollection objsFromProvider( ObjMaskProvider provider, ImageInitParams imageInitParams, LogErrorReporter logErrorReporter ) throws OperationFailedException {

		try {
			ObjMaskProvider objMaskProviderLoc = provider.duplicateBean();
			
			// Initialise
			objMaskProviderLoc.initRecursive(imageInitParams, logErrorReporter);
	
			return objMaskProviderLoc.create(); 
			
		} catch (InitException | CreateException e) {
			throw new OperationFailedException(e);
		}
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

	public List<NamedBean<FeatureListProvider<FeatureInputSingleObj>>> getListFeaturesObjMask() {
		return listFeaturesObjMask;
	}

	public void setListFeaturesObjMask(
			List<NamedBean<FeatureListProvider<FeatureInputSingleObj>>> listFeaturesObjMask) {
		this.listFeaturesObjMask = listFeaturesObjMask;
	}

	public StackProvider getNrgStackProvider() {
		return nrgStackProvider;
	}

	public void setNrgStackProvider(StackProvider nrgStackProvider) {
		this.nrgStackProvider = nrgStackProvider;
	}

	public List<NamedBean<ObjMaskProvider>> getListObjMaskProvider() {
		return listObjMaskProvider;
	}

	public void setListObjMaskProvider(
			List<NamedBean<ObjMaskProvider>> listObjMaskProvider) {
		this.listObjMaskProvider = listObjMaskProvider;
	}


	public FlexiFeatureTable<T> getSelectFeaturesObjects() {
		return selectFeaturesObjects;
	}


	public void setSelectFeaturesObjects(FlexiFeatureTable<T> selectFeaturesObjects) {
		this.selectFeaturesObjects = selectFeaturesObjects;
	}

	public KeyValueParamsProvider getNrgParamsProvider() {
		return nrgParamsProvider;
	}

	public void setNrgParamsProvider(KeyValueParamsProvider nrgParamsProvider) {
		this.nrgParamsProvider = nrgParamsProvider;
	}

	public String getNrgParamsName() {
		return nrgParamsName;
	}

	public void setNrgParamsName(String nrgParamsName) {
		this.nrgParamsName = nrgParamsName;
	}
}
