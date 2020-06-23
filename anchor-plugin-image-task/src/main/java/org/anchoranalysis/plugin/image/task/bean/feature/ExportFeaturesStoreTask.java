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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.NonEmpty;
import org.anchoranalysis.bean.error.BeanDuplicateException;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.results.ResultsVector;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.io.csv.StringLabelsForCsvRow;
import org.anchoranalysis.feature.io.csv.name.SimpleName;
import org.anchoranalysis.feature.io.csv.MetadataHeaders;
import org.anchoranalysis.feature.list.NamedFeatureStore;
import org.anchoranalysis.feature.list.NamedFeatureStoreFactory;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.plugin.image.task.sharedstate.SharedStateExportFeaturesWithStore;

/**
 * Base class for exporting features, where features are calculated per-image
 *   using a NamedFeatureStore
 *   
 * @author FEEHANO
 * 
 * @param T input-manager type
 * @param S feature-params type
 *
 */
public abstract class ExportFeaturesStoreTask<T extends InputFromManager, S extends FeatureInput> extends ExportFeaturesTask<T,SharedStateExportFeaturesWithStore<S>> {

	private static final NamedFeatureStoreFactory STORE_FACTORY = NamedFeatureStoreFactory.factoryParamsOnly();
	
	// START BEAN PROPERTIES
	@BeanField @NonEmpty
	private List<NamedBean<FeatureListProvider<S>>> listFeatures = new ArrayList<>();
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
	protected SharedStateExportFeaturesWithStore<S> createSharedState( MetadataHeaders metadataHeaders, BoundIOContext context) throws CreateException {
		try {
			NamedFeatureStore<S> featureStore = STORE_FACTORY.createNamedFeatureList(listFeatures);
			
			return new SharedStateExportFeaturesWithStore<>(
				metadataHeaders,
				featureStore,
				context
			);
		} catch (AnchorIOException e) {
			throw new CreateException(e);
		}
	}
	
	@Override
	protected String[] headersForResults() {
		return new String[]{firstResultHeader};
	}
	
	@Override
	protected String[] headersForGroup(boolean groupGeneratorDefined) {
		if (groupGeneratorDefined) {
			return new String[]{"group"};
		} else {
			return new String[]{};
		}
	}
	
	@Override
	protected void calcAllResultsForInput(
		InputBound<T,SharedStateExportFeaturesWithStore<S>> input,
		Optional<String> groupGeneratorName
	) throws OperationFailedException {
		try {
			ResultsVector results = calcResultsVectorForInputObject(
				input.getInputObject(),
				input.getSharedState().getFeatureStore(),
				input.context()
			);
			
			input.getSharedState().addResultsFor(
				identifierFor(input.getInputObject().descriptiveName(), groupGeneratorName),
				results
			);
			
		} catch (BeanDuplicateException | FeatureCalcException e) {
			throw new OperationFailedException(e);
		}
	}
	
	protected abstract ResultsVector calcResultsVectorForInputObject(
		T inputObject,
		NamedFeatureStore<S> featureStore,
		BoundIOContext context
	) throws FeatureCalcException;
	
	private static StringLabelsForCsvRow identifierFor(String descriptiveName, Optional<String> groupGeneratorName) throws OperationFailedException {
		return new StringLabelsForCsvRow(
			Optional.of(
				new String[]{descriptiveName}
			),
			groupGeneratorName.map(SimpleName::new)
		);
	}
	
	public List<NamedBean<FeatureListProvider<S>>> getListFeatures() {
		return listFeatures;
	}

	public void setListFeatures(
			List<NamedBean<FeatureListProvider<S>>> listFeatures) {
		this.listFeatures = listFeatures;
	}
}
