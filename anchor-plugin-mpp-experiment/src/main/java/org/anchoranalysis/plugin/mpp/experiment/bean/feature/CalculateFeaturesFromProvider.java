package org.anchoranalysis.plugin.mpp.experiment.bean.feature;

/*-
 * #%L
 * anchor-plugin-mpp-experiment
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

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.results.ResultsVector;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.io.csv.StringLabelsForCsvRow;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorMulti;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.objectmask.ObjectCollection;
import org.anchoranalysis.plugin.image.feature.bean.obj.table.FeatureTableObjs;

class CalculateFeaturesFromProvider<T extends FeatureInput> {

	private final FeatureTableObjs<T> table;
	private final FeatureCalculatorMulti<T> calculator;
	private final BiConsumer<StringLabelsForCsvRow,ResultsVector> addResultsFor;
	private final ImageInitParams imageInitParams;
	private final NRGStackWithParams nrgStack;
	private final boolean suppressErrors;
	private final LogErrorReporter logger;
	
	public CalculateFeaturesFromProvider(
		FeatureTableObjs<T> table,
		FeatureCalculatorMulti<T> calculator,
		BiConsumer<StringLabelsForCsvRow,ResultsVector> addResultsFor,
		ImageInitParams imageInitParams,
		NRGStackWithParams nrgStack,
		boolean suppressErrors,
		LogErrorReporter logger
	) {
		super();
		this.table = table;
		this.calculator = calculator;
		this.addResultsFor = addResultsFor;
		this.imageInitParams = imageInitParams;
		this.nrgStack = nrgStack;
		this.suppressErrors = suppressErrors;
		this.logger = logger;
	}
	
	public void processProvider(
		ObjMaskProvider provider,
		Function<T, StringLabelsForCsvRow> identifierFromInput
	) throws OperationFailedException {
		calculateFeaturesForProvider(
			objsFromProvider(provider, imageInitParams, logger),
			nrgStack,
			identifierFromInput
		);		
	}
	
	private void calculateFeaturesForProvider(
		ObjectCollection objs,
		NRGStackWithParams nrgStack,
		Function<T, StringLabelsForCsvRow> identifierFromInput
	) throws OperationFailedException {
		try {
			List<T> listParams = table.createListInputs(
				objs,
				nrgStack,
				logger
			);
			
			calculateManyFeaturesInto(
				listParams,
				identifierFromInput,
				suppressErrors,
				logger
			);
		} catch (CreateException | OperationFailedException e) {
			throw new OperationFailedException(e);
		}
	}
	
	/**
	 * Calculates a bunch of features with an objectID (unique) and a groupID and adds them to the stored-results
	 * 
	 * The stored-results also have an additional first-column with the ID.
	 * 
	 * @param session for calculating features
	 * @param listInputs 	a list of parameters. Each parameters creates a new result (e.g. a new row in a feature-table)
	 * @param resultsConsumer called with the results
	 * @param extractIdentifier extracts an identifier from each object that is calculated
	 * @param suppressErrors iff TRUE no exceptions are thrown when an error occurs, but rather a message is written to the log
	 * @param logger the log
	 * @throws OperationFailedException
	 */
	private void calculateManyFeaturesInto(
		List<T> listInputs,
		Function<T, StringLabelsForCsvRow> identifierFromObjName,
		boolean suppressErrors,
		LogErrorReporter logger
	) throws OperationFailedException {

		try {
			for(int i=0; i<listInputs.size(); i++ ) {
				
				T input = listInputs.get(i);
			
				logger.getLogReporter().logFormatted("Calculating input %d of %d: %s", i+1, listInputs.size(), input.toString() );
				
				addResultsFor.accept(
					identifierFromObjName.apply(input),
					calculator.calc(input, logger.getErrorReporter(), suppressErrors)
				);
			}
			
		} catch (FeatureCalcException e) {
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
}
