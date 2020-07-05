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
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.feature.shared.SharedFeaturesInitParams;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;

class ExtractFromProvider {
	
	private ExtractFromProvider() {}
	
	public static NRGStackWithParams extractStack( StackProvider nrgStackProvider, ImageInitParams initParams, LogErrorReporter logger ) throws OperationFailedException {
		
		try {
			// Extract the NRG stack
			StackProvider nrgStackProviderLoc = nrgStackProvider.duplicateBean();
			nrgStackProviderLoc.initRecursive(initParams, logger);
			
			return new NRGStackWithParams(nrgStackProviderLoc.create());
		} catch (InitException | CreateException e) {
			throw new OperationFailedException(e);
		}
	}
	
	/** Creates and initializes a single-feature that is provided via a featureProvider */
	public static <T extends FeatureInput> Feature<T> extractFeature(
		FeatureListProvider<T> featureProvider,
		String featureProviderName,
		SharedFeaturesInitParams initParams,
		LogErrorReporter logger
	) throws FeatureCalcException {

		try {
			featureProvider.initRecursive( initParams, logger );

			FeatureList<T> fl = featureProvider.create();
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
}
