package org.anchoranalysis.plugin.image.bean.obj.merge;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.image.feature.bean.evaluator.FeatureEvaluator;

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

import org.anchoranalysis.image.feature.evaluator.PayloadCalculator;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.plugin.image.obj.merge.priority.AssignPriority;
import org.anchoranalysis.plugin.image.obj.merge.priority.AssignPriorityFromImprovement;


/**
 * Merges neighbors if there is an increase in the payload i.e. <pre>if payload(merged) >= avg( payload(src), payload(dest )</pre>
 * 
 * <p>These merges occur in order of the maximum improvement offered.</p>
 * 
 * @author Owen Feehan
 */
public class ObjMaskProviderMergeMax extends ObjMaskProviderMergeWithFeature {

	// START BEAN PROPERTIES
	@BeanField
	private FeatureEvaluator<FeatureInputSingleObject> featureEvaluator;
	// END BEAN PROPERTIES
	
	@Override
	protected PayloadCalculator createPayloadCalculator() throws OperationFailedException {
		
		FeatureCalculatorSingle<FeatureInputSingleObject> calculator = featureEvaluator.createAndStartSession();
		
		return om -> calculator.calc(
			new FeatureInputSingleObject(om)
		);
	}
	
	@Override
	protected AssignPriority createPrioritizer() throws OperationFailedException {
		return new AssignPriorityFromImprovement(
			createPayloadCalculator()	
		);
	}

	@Override
	protected boolean isPlayloadUsed() {
		return true;
	}
	
	public FeatureEvaluator<FeatureInputSingleObject> getFeatureEvaluator() {
		return featureEvaluator;
	}

	public void setFeatureEvaluator(FeatureEvaluator<FeatureInputSingleObject> featureEvaluator) {
		this.featureEvaluator = featureEvaluator;
	}
}
