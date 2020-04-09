package org.anchoranalysis.bean.provider.objs.merge;

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

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Optional;
import org.anchoranalysis.bean.provider.objs.merge.condition.IncreaseFeatureCondition;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.feature.bean.evaluator.FeatureEvaluatorNrgStack;
import org.anchoranalysis.image.objmask.ObjMaskCollection;

public class ObjMaskProviderMerge extends ObjMaskProviderMergeOptionalDistance {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private boolean replaceWithMidpoint = false;
	
	@BeanField @Optional
	private FeatureEvaluatorNrgStack featureEvaluator;
	// END BEAN PROPERTIES

	@Override
	public ObjMaskCollection create() throws CreateException {
		
		ObjMaskCollection objsSource = getObjs().create();
		
		try {
			Merger merger = new Merger(
				replaceWithMidpoint,
				maybeDistanceCondition(),
				new IncreaseFeatureCondition(featureEvaluator),
				calcRes(),
				getLogger()
			);
			
			return mergeMultiplex(
				objsSource,
				a -> merger.tryMerge( a )
			);
			
		} catch (OperationFailedException e) {
			throw new CreateException(e);
		}
	}
	
	public FeatureEvaluatorNrgStack getFeatureEvaluator() {
		return featureEvaluator;
	}

	public void setFeatureEvaluator(FeatureEvaluatorNrgStack featureEvaluator) {
		this.featureEvaluator = featureEvaluator;
	}

	public boolean isReplaceWithMidpoint() {
		return replaceWithMidpoint;
	}

	public void setReplaceWithMidpoint(boolean replaceWithMidpoint) {
		this.replaceWithMidpoint = replaceWithMidpoint;
	}
}
