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

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.ResultsVector;
import org.anchoranalysis.feature.init.FeatureInitParams;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.feature.session.SessionFactory;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorMulti;
import org.anchoranalysis.feature.shared.SharedFeatureSet;
import org.anchoranalysis.feature.shared.SharedFeaturesInitParams;
import org.anchoranalysis.image.feature.stack.FeatureStackParams;

class HelperImageFeatureCalculator {
	
	private LogErrorReporter logErrorReporter;
		
	public HelperImageFeatureCalculator(LogErrorReporter logErrorReporter) {
		super();
		this.logErrorReporter = logErrorReporter;
	}
	

	/** Places all image-features in the SharedObjects and calculates them indirectly via another 'gateway' feature which may call them */
	public double calcSingleIndirectly(
		FeatureListProvider<FeatureStackParams> gatewayFeatureProvider,
		String gatewayFeatureProviderName,
		SharedFeaturesInitParams featureInitParams,
		NRGStackWithParams nrgStack,
		FeatureList<FeatureStackParams> sharedFeatures
	) throws FeatureCalcException {
		
		Feature<FeatureStackParams> feature = singleFeatureFromProvider(
			gatewayFeatureProvider,
			gatewayFeatureProviderName,
			featureInitParams,
			sharedFeatures
		);
		
		return calcInternal(
			nrgStack,
			new FeatureList<>(feature),
			featureInitParams.getSharedFeatureSet().downcast()
		).get(0);
	}
	
	
	
	/** Calculates all image-features directly, returning the results as a vector */
	public ResultsVector calcAllDirectly(
		SharedFeaturesInitParams featureInitParams,
		NRGStackWithParams nrgStack,
		FeatureList<FeatureStackParams> features
	) throws FeatureCalcException {
		return calcInternal(
			nrgStack,
			features,
			featureInitParams.getSharedFeatureSet().downcast()
		);
	}
	


	

	/** Initializes a feature with shared-objects that
	 *  also have a list of other features added (as duplicates) 
	 * @throws InitException */
	private void initFeatureProviderWithSharedFeatures(
		FeatureListProvider<FeatureStackParams> provider,
		SharedFeaturesInitParams initParams,
		FeatureList<FeatureStackParams> sharedFeatures
	) throws InitException {
		
		provider.initRecursive( initParams, logErrorReporter );
		
		// Add our image-features to the shared feature set
		// This must be done before calling create() on classifierProvider
		initParams.getSharedFeatureSet().addDuplicate(sharedFeatures.downcast());
	}
	
	

	/** Creates and initializes a single-feature that is provided via a featureProvider */
	private Feature<FeatureStackParams> singleFeatureFromProvider(
		FeatureListProvider<FeatureStackParams> featureProvider,
		String featureProviderName,
		SharedFeaturesInitParams initParams,
		FeatureList<FeatureStackParams> sharedFeatures
	) throws FeatureCalcException {

		try {
			initFeatureProviderWithSharedFeatures(
				featureProvider,
				initParams,
				sharedFeatures
			);

			FeatureList<FeatureStackParams> fl = featureProvider.create();
			if (fl.size()!=1) {
				throw new FeatureCalcException(
					String.format("%s must return exactly one feature from its list. It currently returns %d", featureProviderName, fl.size() )
				);
			}
			return fl.get(0);
		} catch (CreateException | InitException e) {
			throw new FeatureCalcException(e);
		}
	}
		
	private ResultsVector calcInternal(
			NRGStackWithParams stack,
			FeatureList<FeatureStackParams> featuresDirectlyCalculate,
			SharedFeatureSet<FeatureStackParams> sharedFeatures
		) throws FeatureCalcException {
		
		FeatureCalculatorMulti<FeatureStackParams> session = SessionFactory.createAndStart(
			featuresDirectlyCalculate,
			new FeatureInitParams(),
			sharedFeatures,
			logErrorReporter
		);
		
		return session.calcOne(
			new FeatureStackParams(stack.getNrgStack())
		);
	}

}
