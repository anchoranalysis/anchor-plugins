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
import java.util.function.BiConsumer;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.calc.results.ResultsVector;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.io.csv.MetadataHeaders;
import org.anchoranalysis.feature.io.csv.StringLabelsForCsvRow;
import org.anchoranalysis.feature.io.csv.name.CombinedName;
import org.anchoranalysis.feature.io.csv.name.MultiName;
import org.anchoranalysis.feature.io.csv.name.SimpleName;
import org.anchoranalysis.feature.io.csv.GroupedResultsVectorCollection;
import org.anchoranalysis.feature.list.NamedFeatureStoreFactory;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.feature.session.FeatureTableSession;
import org.anchoranalysis.image.init.ImageInitParams;
import org.anchoranalysis.image.objectmask.ObjectCollection;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.mpp.io.input.MultiInput;
import org.anchoranalysis.mpp.sgmn.bean.define.DefineOutputterMPPWithNrg;
import org.anchoranalysis.plugin.image.feature.bean.obj.table.FeatureTableObjs;
import org.anchoranalysis.plugin.image.task.bean.feature.ExportFeaturesTask;
import org.anchoranalysis.plugin.image.task.sharedstate.SharedStateExportFeatures;


/** 
 * Calculates features for each object in a collection.
 *
 *  <ol>
 *  <li>All files are aggregated into groups (with the name of the ObjMaskProvider added to the end)</li>
 *  <li>For each image file, the <code>define</code> is applied and an {@link ObjectCollection} is extracted</li>
 *  <li>These objects are added to the appropriate {@link ObjectCollection} associated with each group</li>
 *  <li>Various exports may occur with either the features for each object (or aggregated features for the groups)</li>
 *  </ol>
 *  
 *  <div>
 *  These exports are:
 *  <table>
 *  <tr><td>features</td><td>a single csv file where each row is an object</td></tr>
 *  <tr><td>featuresAggregated</td><td>a single csv file where each row is a group (with aggregated features of the objects within)</td></tr>
 *  <tr><td>featuresGroup</td><td>a csv file per group, where each row is an object</td></tr>
 *  </table>
 *  </div>
 *  
 *  <p>Note unlike other export-tasks, the group here is not only what is returned by the <code>group</code> generator
 *  in the super-class, but also includes the name of the {@link ObjMaskProvider} if there is more than one.</p>
 *  
 *  TODO does this need to be a MultiInput and dependent on MPP? Can it be moved to anchor-plugin-image-task??
 *  
 *  @param T the feature input-type supported by the FlexiFeatureTable
**/
public class ExportFeaturesObjMaskTask<T extends FeatureInput> extends ExportFeaturesTask<MultiInput,SharedStateExportFeaturesObjMask<T>> {

	private static final NamedFeatureStoreFactory STORE_FACTORY = NamedFeatureStoreFactory.bothNameAndParams();
	
	// START BEAN PROPERTIES
	@BeanField
	private DefineOutputterMPPWithNrg define = new DefineOutputterMPPWithNrg();
	
	@BeanField
	private List<NamedBean<FeatureListProvider<FeatureInputSingleObj>>> listFeaturesObjMask = new ArrayList<>();
	
	@BeanField
	private List<NamedBean<ObjMaskProvider>> listObjMaskProvider = new ArrayList<>();
	
	@BeanField
	private FeatureTableObjs<T> table;
	
	/**
	 * If non-empty, A keyValueParams is treated as part of the nrgStack 
	 */
	@BeanField @AllowEmpty
	private String nrgParamsName = "";
	
	// START BEAN PROPERTIES
	@BeanField
	private boolean suppressErrors = false;
	//END BEAN PROPERTIES
	
	@Override
	public SharedStateExportFeaturesObjMask<T> beforeAnyJobIsExecuted(
			BoundOutputManagerRouteErrors outputManager,
			ParametersExperiment params
	) throws ExperimentExecutionException {
		
		try {
			FeatureTableSession<T> session = table.createFeatures(
				listFeaturesObjMask,
				STORE_FACTORY,
				suppressErrors
			); 
			return new SharedStateExportFeaturesObjMask<>(
				new GroupedResultsVectorCollection(
					new MetadataHeaders(
						headersForGroup(),
						new String[]{"image", "unique_pixel_in_object"}
					),
					session.createFeatureNames(),
					params.context()
				),
				session
			);
			
		} catch (CreateException | InitException | AnchorIOException e) {
			throw new ExperimentExecutionException(e);
		}
	}
	
	private boolean moreThanOneProvider() {
		return listObjMaskProvider.size()>1;
	}
	
	private String[] headersForGroup() {
		if (isGroupGeneratorDefined()) {
			if (moreThanOneProvider()) {
				return new String[]{"group", "object_collection"};
			} else {
				return new String[]{"group"};
			}
		} else {
			if (moreThanOneProvider()) {
				return new String[]{"object_collection"};
			} else {
				return new String[]{};
			}
		}
	}

	/** If either a group-generator is defined or there's more than one provider, then groups should be included */
	@Override
	protected boolean includeGroupInExperiment() {
		return super.isGroupGeneratorDefined() || moreThanOneProvider();
	}
	
	@Override
	public InputTypesExpected inputTypesExpected() {
		return new InputTypesExpected(MultiInput.class);
	}

	@Override
	public void doJobOnInputObject(	InputBound<MultiInput,SharedStateExportFeaturesObjMask<T>> input ) throws JobExecutionException {
		
		try {
			define.processInput(
				input.getInputObject(),
				input.context(),
				(initParams, nrgStack) -> calculateFeaturesForImage(input, initParams, nrgStack) 
			);
						
		} catch (OperationFailedException e) {
			throw new JobExecutionException(e);
		}
	}
	
	private int calculateFeaturesForImage(
		InputBound<MultiInput,SharedStateExportFeaturesObjMask<T>> input,
		ImageInitParams imageInit,
		NRGStackWithParams nrgStack
	) throws OperationFailedException {
		
		BoundIOContext context = input.context();
		
		// Create a duplicated featureStore for this image, as we want separate features on this thread,
		//  so they are thread-safe, for parallel execution
		FeatureTableSession<T> session = input.getSharedState().getSession().duplicateForNewThread();
		
		try {
			session.start(
				imageInit,
				Optional.of(nrgStack),
				context.getLogger()
			);
		} catch (InitException e) {
			throw new OperationFailedException(e);
		}
					
		processAllProviders(
			input.getInputObject(),
			session,
			imageInit,
			nrgStack,
			input.getSharedState(),
			context
		);
		
		// Arbitrary, we need a return-type
		return 0;
	}

	
	private void processAllProviders(
		MultiInput input,
		FeatureTableSession<T> session,
		ImageInitParams imageInitParams,
		NRGStackWithParams nrgStack,
		SharedStateExportFeatures sharedState,
		BoundIOContext context		
	) throws OperationFailedException {
		try {
			String id = input.descriptiveName();
			
			Optional<String> groupGeneratorName = extractGroupNameFromGenerator(
				input.pathForBindingRequired(),
				context.isDebugEnabled()
			);
									
			// For every objMaskCollection provider
			for( NamedBean<ObjMaskProvider> ni : listObjMaskProvider) {
				calculateFeaturesForProvider(
					objsFromProvider(ni.getValue(), imageInitParams, context.getLogger()),
					session,
					nrgStack,
					(results,objName) -> sharedState.getGroupedResults().addResultsFor(
						new StringLabelsForCsvRow(
							Optional.of(
								new String[]{id, objName}
							),
							createGroupName(groupGeneratorName, ni.getName())
						),
						results
					),
					context.getLogger()
				);
			}
		} catch (AnchorIOException e) {
			throw new OperationFailedException(e);
		}
	}
	
	private Optional<MultiName> createGroupName( Optional<String> groupGeneratorName, String providerName ) {
		if (moreThanOneProvider()) {
			if (groupGeneratorName.isPresent()) {
				return Optional.of(
					new CombinedName(groupGeneratorName.get(), providerName)
				);
			} else {
				return Optional.of(
					new SimpleName(providerName)
				);
			}
		} else {
			return groupGeneratorName.map(SimpleName::new);
		}
	}
		
	private void calculateFeaturesForProvider(
		ObjectCollection objs,
		FeatureTableSession<T> session,
		NRGStackWithParams nrgStack,
		BiConsumer<ResultsVector,String> resultsConsumer,
		LogErrorReporter logErrorReporter
	) throws OperationFailedException {
		try {
			List<T> listParams = table.createListInputs(
				objs,
				nrgStack,
				logErrorReporter
			);
			
			FeatureCalculator.calculateManyFeaturesInto(
				session,
				listParams,
				resultsConsumer,
				suppressErrors,
				logErrorReporter
			);
		} catch (CreateException | OperationFailedException e) {
			throw new OperationFailedException(e);
		}
	}
	
	private static ObjectCollection objsFromProvider( ObjMaskProvider provider, ImageInitParams imageInitParams, LogErrorReporter logErrorReporter ) throws OperationFailedException {

		try {
			ObjMaskProvider objMaskProviderLoc = provider.duplicateBean();
			
			// Initialise
			objMaskProviderLoc.initRecursive(imageInitParams, logErrorReporter);
	
			return objMaskProviderLoc.create(); 
			
		} catch (InitException | CreateException e) {
			throw new OperationFailedException(e);
		}
	}

	public List<NamedBean<FeatureListProvider<FeatureInputSingleObj>>> getListFeaturesObjMask() {
		return listFeaturesObjMask;
	}

	public void setListFeaturesObjMask(
			List<NamedBean<FeatureListProvider<FeatureInputSingleObj>>> listFeaturesObjMask) {
		this.listFeaturesObjMask = listFeaturesObjMask;
	}

	public List<NamedBean<ObjMaskProvider>> getListObjMaskProvider() {
		return listObjMaskProvider;
	}

	public void setListObjMaskProvider(
			List<NamedBean<ObjMaskProvider>> listObjMaskProvider) {
		this.listObjMaskProvider = listObjMaskProvider;
	}

	public String getNrgParamsName() {
		return nrgParamsName;
	}

	public void setNrgParamsName(String nrgParamsName) {
		this.nrgParamsName = nrgParamsName;
	}

	public boolean isSuppressErrors() {
		return suppressErrors;
	}

	public void setSuppressErrors(boolean suppressErrors) {
		this.suppressErrors = suppressErrors;
	}

	public DefineOutputterMPPWithNrg getDefine() {
		return define;
	}

	public void setDefine(DefineOutputterMPPWithNrg define) {
		this.define = define;
	}

	public FeatureTableObjs<T> getTable() {
		return table;
	}

	public void setTable(FeatureTableObjs<T> table) {
		this.table = table;
	}
}
