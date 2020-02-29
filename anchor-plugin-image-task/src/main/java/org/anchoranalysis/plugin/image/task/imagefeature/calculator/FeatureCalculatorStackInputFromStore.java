package org.anchoranalysis.plugin.image.task.imagefeature.calculator;

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

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.ResultsVector;
import org.anchoranalysis.feature.list.NamedFeatureStore;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.init.ImageInitParams;
import org.anchoranalysis.image.io.input.ProvidesStackInput;
import org.anchoranalysis.image.io.input.StackInputInitParamsCreator;

/**
 * Calculates feature or feature values with the following objects:
 *   1. input-stack (stackInputBase)
 *   2. nrgStackProvider (determines an nrgStack passed to the features)
 *   3. featureStore (a pot of features somehow used in the calculations)
 *   
 * @author FEEHANO
 *
 */
public class FeatureCalculatorStackInputFromStore {

	private FeatureList featureList;
	
	private HelperImageFeatureCalculator helper;
	private ImageInitParams initParams;
	private NRGStackWithParams nrgStack;
		
	public FeatureCalculatorStackInputFromStore(ProvidesStackInput stackInput, StackProvider nrgStackProvider,
			NamedFeatureStore featureStore, LogErrorReporter logErrorReporter) throws OperationFailedException {
		super();
		
		helper = new HelperImageFeatureCalculator(logErrorReporter);
		this.initParams = StackInputInitParamsCreator.createInitParams(stackInput, logErrorReporter);
		this.nrgStack = HelperInit.extractStack( initParams, nrgStackProvider, logErrorReporter );
		
		this.featureList = extractFeatures(featureStore);
	}
	
	/** Calculates a single-feature that comes in a featureProvider 
	 * @throws FeatureCalcException */
	public double calcSingleFromProvider(
			FeatureListProvider featureProvider,
			String featureProviderName
	) throws FeatureCalcException {
		return helper.calcSingleIndirectly(
			featureProvider,
			featureProviderName,
			initParams.getFeature(),
			nrgStack,
			featureList
		);
	}
	
	/** Calculates all image-features in the feature-store */
	public ResultsVector calcAllInStore() throws FeatureCalcException {
		return helper.calcAllDirectly(initParams.getFeature(), nrgStack, featureList);
	}

	public ImageInitParams getInitParams() {
		return initParams;
	}
	
	private static FeatureList extractFeatures( NamedFeatureStore featureStore ) {
		NamedFeatureStore featureStoreForTask = featureStore.deepCopy();
		return featureStoreForTask.listFeatures();
	}
	
}
