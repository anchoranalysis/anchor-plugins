package org.anchoranalysis.bean.provider.objs.merge;

import org.anchoranalysis.bean.provider.objs.merge.MergeGraph.AssignPriority;

/*
 * #%L
 * anchor-image-feature
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


import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.evaluator.EvaluateSingleObjMask;
import org.anchoranalysis.image.objmask.ObjMask;

import objmaskwithfeature.ObjMaskWithFeature;
import objmaskwithfeature.ObjMaskWithFeatureAndPriority;

class ImprovementPriority implements AssignPriority {
	public ObjMaskWithFeatureAndPriority assignPriority(
		ObjMaskWithFeature omSrcWithFeature,
		ObjMaskWithFeature omDestWithFeature,
		ObjMask omMerge,
		EvaluateSingleObjMask featureEvaluatorSingleObj,
		LogErrorReporter logErrorReporter
	) throws OperationFailedException
	{
		double featureMerge = calcFeatureVal(featureEvaluatorSingleObj, omMerge);
		double featureAvg = weightedAverageFeatureVal(omSrcWithFeature, omDestWithFeature);
		double improvement = featureMerge - featureAvg;
		
		if (featureMerge<=featureAvg) {	
			// We add an edge anyway, as a placeholder as future merges might make it viable
			return new ObjMaskWithFeatureAndPriority(omMerge,featureMerge,improvement,false);
		} 
		
		return new ObjMaskWithFeatureAndPriority(omMerge,featureMerge,improvement,true);
	}
	
	private double calcFeatureVal( EvaluateSingleObjMask featureEvaluatorSingleObj, ObjMask om ) throws OperationFailedException {
		try {
			return featureEvaluatorSingleObj.calc(om);
		} catch (FeatureCalcException e) {
			throw new OperationFailedException(e);
		}
	}
	
	/** A weighted-average of the feature value (weighted according to the relative number of pixeks */
	private static double weightedAverageFeatureVal( ObjMaskWithFeature om1, ObjMaskWithFeature om2 ) {
		long size1 = om1.numPixels();
		long size2 = om2.numPixels();
		
		double weightedSum = (om1.getFeatureVal()*size1) + (om2.getFeatureVal()*size2);
		return weightedSum / (size1 + size2);
	}
	

	@Override
	public void end() throws OperationFailedException {
		// NOTHING TO DO
	}
}