package org.anchoranalysis.bean.provider.objs.merge.condition;

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
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.feature.session.SequentialSession;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorMulti;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorMultiChangeParams;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.image.extent.ImageRes;
import org.anchoranalysis.image.feature.bean.evaluator.FeatureEvaluator;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.feature.objmask.pair.FeatureObjMaskPairParams;
import org.anchoranalysis.image.feature.objmask.pair.merged.FeatureObjMaskPairMergedParams;
import org.anchoranalysis.image.feature.session.FeatureSessionCreateParamsSingle;
import org.anchoranalysis.image.objmask.ObjMask;

public class IncreaseFeatureCondition implements AfterCondition {

	private FeatureEvaluator<FeatureObjMaskParams> featureEvaluator;
	
	private FeatureCalculatorSingle<FeatureObjMaskParams> session;
	
	public IncreaseFeatureCondition(FeatureEvaluator<FeatureObjMaskParams> featureEvaluator) {
		super();
		this.featureEvaluator = featureEvaluator;
	}

	@Override
	public void init(LogErrorReporter logger) throws InitException {
		
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
	public boolean accept(ObjMask omSrc, ObjMask omDest, ObjMask omMerged, ImageRes res) throws OperationFailedException {
		
		if (session!=null) {
			return doesIncreaseFeatureValueForBoth(omSrc, omDest, omMerged);
		} else {
			return true;
		}
	}
	
	private boolean doesIncreaseFeatureValueForBoth( ObjMask omSrc, ObjMask omDest, ObjMask omMerge ) throws OperationFailedException {
		
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
	
	private double calc(ObjMask om) throws FeatureCalcException {
		return session.calcOne(
			new FeatureObjMaskParams(om)
		);
	}
}
