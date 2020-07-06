package org.anchoranalysis.plugin.image.obj.merge.condition;

import java.util.Optional;

/*-
 * #%L
 * anchor-plugin-image
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

import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.feature.bean.evaluator.FeatureEvaluator;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;

public class IncreaseFeatureCondition implements AfterCondition {

	private FeatureEvaluator<FeatureInputSingleObject> featureEvaluator;
	
	private FeatureCalculatorSingle<FeatureInputSingleObject> session;
	
	public IncreaseFeatureCondition(FeatureEvaluator<FeatureInputSingleObject> featureEvaluator) {
		super();
		this.featureEvaluator = featureEvaluator;
	}

	@Override
	public void init(Logger logger) throws InitException {
		
		if (featureEvaluator!=null) {
			try {
				session = featureEvaluator.createAndStartSession();
			} catch (OperationFailedException e) {
				throw new InitException(e);
			}
		} else {
			session = null;
		}
		
	}

	@Override
	public boolean accept(ObjectMask omSrc, ObjectMask omDest, ObjectMask omMerged, Optional<ImageResolution> res) throws OperationFailedException {
		
		if (session!=null) {
			return doesIncreaseFeatureValueForBoth(omSrc, omDest, omMerged);
		} else {
			return true;
		}
	}
	
	private boolean doesIncreaseFeatureValueForBoth( ObjectMask omSrc, ObjectMask omDest, ObjectMask omMerge ) throws OperationFailedException {
		
		// Feature source
		try {
			double featureSrc = calc(omSrc);
			double featureDest = calc(omDest);
			double featureMerge = calc(omMerge);
			
			// We skip if we don't increase the feature value for both objects
			return (featureMerge>featureSrc && featureMerge>featureDest);
			
		} catch (FeatureCalcException e) {
			throw new OperationFailedException(e);
		}		
	}
	
	private double calc(ObjectMask om) throws FeatureCalcException {
		return session.calc(
			new FeatureInputSingleObject(om)
		);
	}
}
