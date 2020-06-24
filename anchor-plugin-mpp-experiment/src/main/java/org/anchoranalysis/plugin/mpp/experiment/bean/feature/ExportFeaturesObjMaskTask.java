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
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.io.csv.MetadataHeaders;
import org.anchoranalysis.feature.io.csv.StringLabelsForCsvRow;
import org.anchoranalysis.feature.io.csv.name.CombinedName;
import org.anchoranalysis.feature.io.csv.name.MultiName;
import org.anchoranalysis.feature.io.csv.name.SimpleName;
import org.anchoranalysis.feature.list.NamedFeatureStoreFactory;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.feature.session.FeatureTableSession;
import org.anchoranalysis.image.objectmask.ObjectCollection;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.mpp.io.input.MultiInput;
import org.anchoranalysis.mpp.sgmn.bean.define.DefineOutputterMPPWithNrg;
import org.anchoranalysis.plugin.image.feature.bean.obj.table.FeatureTableObjs;
import org.anchoranalysis.plugin.image.task.bean.feature.ExportFeaturesTask;


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
	
	@BeanField
	private boolean suppressErrors = false;
	//END BEAN PROPERTIES

	@Override
	public InputTypesExpected inputTypesExpected() {
		return new InputTypesExpected(MultiInput.class);
	}
	
	@Override
	protected SharedStateExportFeaturesObjMask<T> createSharedState( MetadataHeaders metadataHeaders, BoundIOContext context) throws CreateException {
		try {
			FeatureTableSession<T> session = table.createFeatures(
				listFeaturesObjMask,
				STORE_FACTORY,
				suppressErrors
			); 
			return new SharedStateExportFeaturesObjMask<>(
				metadataHeaders,
				session,
				context
			);
			
		} catch (InitException | AnchorIOException e) {
			throw new CreateException(e);
		}
	}

	@Override
	protected void calcAllResultsForInput(
		InputBound<MultiInput,SharedStateExportFeaturesObjMask<T>> input,
		Optional<String> groupGeneratorName
	) throws OperationFailedException {
		define.processInput(
			input.getInputObject(),
			input.context(),
			(initParams, nrgStack) -> calculateFeaturesForImage(input, groupGeneratorName, initParams, nrgStack) 
		);
	}

	@Override
	protected String[] headersForResults() {
		return new String[]{"image", "unique_pixel_in_object"};
	}
	
	@Override
	protected String[] headersForGroup(boolean groupGeneratorDefined) {
		boolean moreThanOneProvider = moreThanOneProvider();
		if (groupGeneratorDefined) {
			if (moreThanOneProvider) {
				return new String[]{"group", "object_collection"};
			} else {
				return new String[]{"group"};
			}
		} else {
			if (moreThanOneProvider) {
				return new String[]{"object_collection"};
			} else {
				return new String[]{};
			}
		}
	}

	/** If either a group-generator is defined or there's more than one provider, then groups should be included */
	@Override
	protected boolean includeGroupInExperiment(boolean groupGeneratorDefined) {
		return groupGeneratorDefined || moreThanOneProvider();
	}
	
	private boolean moreThanOneProvider() {
		return listObjMaskProvider.size()>1;
	}
	
	private int calculateFeaturesForImage(
		InputBound<MultiInput,SharedStateExportFeaturesObjMask<T>> input,
		Optional<String> groupGeneratorName,
		ImageInitParams imageInit,
		NRGStackWithParams nrgStack
	) throws OperationFailedException {
		
		FeatureTableSession<T> session = duplicateAndStartSession(input, imageInit, nrgStack);
		FeatureCalculator<T> calculator = new FeatureCalculator<>(
			table,
			input.getSharedState(),
			imageInit,
			nrgStack,
			suppressErrors,
			input.context().getLogger()
		);
		processAllProviders(
			input.getInputObject().descriptiveName(),
			groupGeneratorName,
			session,
			calculator
		);
		
		// Arbitrary, we need a return-type
		return 0;
	}
	
	private void processAllProviders(
		String descriptiveName,
		Optional<String> groupGeneratorName,
		FeatureTableSession<T> session,
		FeatureCalculator<T> calculator
	) throws OperationFailedException {
		
		// For every objMaskCollection provider
		for(NamedBean<ObjMaskProvider> ni : listObjMaskProvider) {
			calculator.processProvider(
				ni.getValue(),
				session,
				objName -> identifierFor(descriptiveName, objName, groupGeneratorName, ni.getName())
			);
		}
	}
	
	private StringLabelsForCsvRow identifierFor(
		String descriptiveName,
		String objName,
		Optional<String> groupGeneratorName,
		String providerName
	) {
		return new StringLabelsForCsvRow(
			Optional.of(
				new String[]{descriptiveName, objName}
			),
			createGroupName(groupGeneratorName, providerName)
		);
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

	private FeatureTableSession<T> duplicateAndStartSession(
		InputBound<MultiInput,SharedStateExportFeaturesObjMask<T>> input,
		ImageInitParams imageInit,
		NRGStackWithParams nrgStack
	) throws OperationFailedException {
		
		// Create a duplicated featureStore for this image, as we want separate features on this thread,
		//  so they are thread-safe, for parallel execution
		FeatureTableSession<T> session = input.getSharedState().getSession().duplicateForNewThread();
		
		try {
			session.start(
				imageInit,
				Optional.of(nrgStack),
				input.context().getLogger()
			);
		} catch (InitException e) {
			throw new OperationFailedException(e);
		}
		
		return session;
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
