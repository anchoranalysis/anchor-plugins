package org.anchoranalysis.bean.provider.objs.merge;

import org.anchoranalysis.feature.session.SequentialSession;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorMultiChangeParams;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;

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

import org.anchoranalysis.image.feature.evaluator.EvaluateSingleObjMask;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;

// Merges with the object that maximises a feature
public class ObjMaskProviderMergeMax extends ObjMaskProviderMergeWithFeature {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	protected EvaluateSingleObjMask featureEvaluator(FeatureCalculatorSingle<FeatureObjMaskParams> featureSession) {
		
		return om -> {
			FeatureObjMaskParams params = new FeatureObjMaskParams(om);
			return featureSession.calcOne(params);
		};
	}
	
	@Override
	protected MergeGraph.AssignPriority assignPriority() {
		return new ImprovementPriority();
	}

	@Override
	protected boolean isSingleFeatureUsed() {
		return true;
	}
}
