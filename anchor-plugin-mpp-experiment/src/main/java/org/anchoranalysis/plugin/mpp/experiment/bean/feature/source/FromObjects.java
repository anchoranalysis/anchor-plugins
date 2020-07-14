package org.anchoranalysis.plugin.mpp.experiment.bean.feature.source;

/*-
 * #%L
 * anchor-plugin-mpp-experiment
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan
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
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.calc.results.ResultsVector;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.io.csv.MetadataHeaders;
import org.anchoranalysis.feature.io.csv.StringLabelsForCsvRow;
import org.anchoranalysis.feature.io.csv.name.CombinedName;
import org.anchoranalysis.feature.io.csv.name.MultiName;
import org.anchoranalysis.feature.io.csv.name.SimpleName;
import org.anchoranalysis.feature.list.NamedFeatureStoreFactory;
import org.anchoranalysis.feature.name.FeatureNameList;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorMulti;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.feature.session.FeatureTableCalculator;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.mpp.io.input.MultiInput;
import org.anchoranalysis.mpp.sgmn.bean.define.DefineOutputterMPPWithNrg;
import org.anchoranalysis.plugin.image.feature.bean.object.table.FeatureTableObjects;
import org.anchoranalysis.plugin.image.task.bean.feature.source.FeatureSource;
import org.anchoranalysis.plugin.image.task.feature.GenerateHeadersForCSV;
import org.anchoranalysis.plugin.image.task.feature.SharedStateExportFeatures;

/** 
 * Extracts features for each object in a collection.
 *
 *  <ol>
 *  <li>All input are aggregated into groups (with the name of the ObjMaskProvider added to the end)</li>
 *  <li>For each input, the <code>define</code> is applied and one or more {@link ObjectCollection} are extracted</li>
 *  <li>These objects are added to the appropriate {@link ObjectCollection} associated with each group</li>
 *  </ol>
 *  
 *  <p>Note unlike other feature-sources, the group here is not only what is returned by the <code>group</code> generator
 *  in the super-class, but also includes the name of the {@link ObjectCollectionProvider} if there is more than one.</p>
 *  
 *  TODO does this need to be a MultiInput and dependent on MPP? Can it be moved to anchor-plugin-image-task??
 *  
 *  @param <T> the feature input-type supported by the FlexiFeatureTable
**/
public class FromObjects<T extends FeatureInput> extends FeatureSource<MultiInput,FeatureTableCalculator<T>,FeatureInputSingleObject> {

	private static final NamedFeatureStoreFactory STORE_FACTORY = NamedFeatureStoreFactory.bothNameAndParams();
	
	// START BEAN PROPERTIES
	@BeanField
	private DefineOutputterMPPWithNrg define = new DefineOutputterMPPWithNrg();
	
	@BeanField
	private List<NamedBean<ObjectCollectionProvider>> listObjMaskProvider = new ArrayList<>();
	
	@BeanField
	private FeatureTableObjects<T> table;
	
	@BeanField
	private boolean suppressErrors = false;
	//END BEAN PROPERTIES

	@Override
	public InputTypesExpected inputTypesExpected() {
		return new InputTypesExpected(MultiInput.class);
	}
	
	@Override
	public SharedStateExportFeatures<FeatureTableCalculator<T>> createSharedState( MetadataHeaders metadataHeaders, List<NamedBean<FeatureListProvider<FeatureInputSingleObject>>> features, BoundIOContext context) throws CreateException {
		try {
			FeatureTableCalculator<T> tableCalculator = table.createFeatures(
				features,
				STORE_FACTORY,
				suppressErrors
			);
			return new SharedStateExportFeatures<>(
				metadataHeaders,
				tableCalculator.createFeatureNames(),
				tableCalculator::duplicateForNewThread,
				context
			);
		} catch (InitException | AnchorIOException e) {
			throw new CreateException(e);
		}
	}

	@Override
	public void calcAllResultsForInput(
		MultiInput input,
		BiConsumer<StringLabelsForCsvRow,ResultsVector> addResultsFor,
		FeatureTableCalculator<T> featureSourceSupplier,
		FeatureNameList featureNames,
		Optional<String> groupGeneratorName,
		BoundIOContext context
	) throws OperationFailedException {
		define.processInput(
			input,
			context,
			(initParams, nrgStack) -> calculateFeaturesForImage(
				input.descriptiveName(),
				featureSourceSupplier,
				addResultsFor,
				groupGeneratorName,
				initParams,
				nrgStack,
				context.getLogger()
			) 
		);
	}
	
	@Override
	public GenerateHeadersForCSV headers() {
		return new GenerateHeadersForCSV(
			new String[]{"image", "unique_pixel_in_object"},
			moreThanOneProvider() ? Optional.of("object_collection") : Optional.empty()
		);
	}

	/** If either a group-generator is defined or there's more than one provider, then groups should be included */
	@Override
	public boolean includeGroupInExperiment(boolean groupGeneratorDefined) {
		return groupGeneratorDefined || moreThanOneProvider();
	}
	
	private boolean moreThanOneProvider() {
		return listObjMaskProvider.size()>1;
	}
	
	private int calculateFeaturesForImage(
		String descriptiveName,
		FeatureTableCalculator<T> calculator,
		BiConsumer<StringLabelsForCsvRow,ResultsVector> addResultsFor,
		Optional<String> groupGeneratorName,
		ImageInitParams imageInit,
		NRGStackWithParams nrgStack,
		Logger logger
	) throws OperationFailedException {
		
		CalculateFeaturesFromProvider<T> fromProviderCalculator = new CalculateFeaturesFromProvider<>(
			table,
			startCalculator(
				calculator,
				imageInit,
				nrgStack,
				logger
			),
			addResultsFor,
			imageInit,
			nrgStack,
			suppressErrors,
			logger
		);
		processAllProviders(
			descriptiveName,
			groupGeneratorName,
			fromProviderCalculator
		);
		
		// Arbitrary, we need a return-type
		return 0;
	}
	
	private FeatureCalculatorMulti<T> startCalculator(
		FeatureTableCalculator<T> calculator,
		ImageInitParams imageInit,
		NRGStackWithParams nrgStack,
		Logger logger
	) throws OperationFailedException {
		
		try {
			calculator.start(
				imageInit,
				Optional.of(nrgStack),
				logger
			);
		} catch (InitException e) {
			throw new OperationFailedException(e);
		}
		
		return calculator;
	}
	
	private void processAllProviders(
		String descriptiveName,
		Optional<String> groupGeneratorName,
		CalculateFeaturesFromProvider<T> calculator
	) throws OperationFailedException {
		
		// For every object-collection-provider
		for(NamedBean<ObjectCollectionProvider> ni : listObjMaskProvider) {
			calculator.processProvider(
				ni.getValue(),
				input -> identifierFor(
					descriptiveName,
					table.uniqueIdentifierFor(input),
					groupGeneratorName,
					ni.getName()
				)
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

	public List<NamedBean<ObjectCollectionProvider>> getListObjMaskProvider() {
		return listObjMaskProvider;
	}

	public void setListObjMaskProvider(
			List<NamedBean<ObjectCollectionProvider>> listObjMaskProvider) {
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

	public FeatureTableObjects<T> getTable() {
		return table;
	}

	public void setTable(FeatureTableObjects<T> table) {
		this.table = table;
	}
}
