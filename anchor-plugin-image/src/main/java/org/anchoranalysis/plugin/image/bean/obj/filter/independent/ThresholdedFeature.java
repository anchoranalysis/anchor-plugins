package org.anchoranalysis.plugin.image.bean.obj.filter.independent;

import java.util.Optional;

/*
 * #%L
 * anchor-plugin-image-feature
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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.relation.RelationToValue;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.feature.bean.evaluator.FeatureEvaluator;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.obj.filter.ObjectFilterRelation;

/**
 * Only keeps objects whose feature-value satisfies a condition relative to a threshold.
 * 
 * <p>Specifically, <code>relation(featureValue,threshold)</code> must be true.
 * 
 * @author Owen Feehan
 *
 */
public class ThresholdedFeature extends ObjectFilterRelation {

	// START BEAN PROPERTIES
	@BeanField
	private FeatureEvaluator<FeatureInputSingleObject> featureEvaluator;
	
	@BeanField
	private double threshold;
	
	@BeanField
	private boolean debug = true;
	// END BEAN PROPERTIES
	
	private FeatureCalculatorSingle<FeatureInputSingleObject> featureSession = null;
		
	@Override
	protected void start(Optional<ImageDimensions> dim, ObjectCollection objsToFilter) throws OperationFailedException {
		super.start(dim, objsToFilter);
		
		// This could be called many times, so we create a new feature session only on the first occasion
		if (featureSession==null) {
			featureSession = featureEvaluator.createAndStartSession();
		}
		
		if (debug) {
			getLogger().getLogReporter().log( String.format("START Feature Threshold") );
		}
	}

	@Override
	protected boolean match(ObjectMask om, Optional<ImageDimensions> dim, RelationToValue relation) throws OperationFailedException {

		double val;
		try {
			val = featureSession.calc(
				new FeatureInputSingleObject(om)
			);
		} catch (FeatureCalcException e) {
			throw new OperationFailedException(e);
		}

		boolean succ = (relation.isRelationToValueTrue(val, threshold));
		
		if(debug) {
			if (succ) {
				getLogger().getLogReporter().logFormatted("%s\tVal=%f\tthreshold=%f\t  (Accepted)", om.centerOfGravity(), val, threshold);
			} else {
				getLogger().getLogReporter().logFormatted("%s\tVal=%f\tthreshold=%f", om.centerOfGravity(), val, threshold);
			}
		}
		
		return succ;
	}

	@Override
	protected void end() throws OperationFailedException {
		super.end();
		if (debug) {
			getLogger().getLogReporter().log( String.format("END Feature Threshold") );
		}
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public FeatureEvaluator<FeatureInputSingleObject> getFeatureEvaluator() {
		return featureEvaluator;
	}

	public void setFeatureEvaluator(FeatureEvaluator<FeatureInputSingleObject> featureEvaluator) {
		this.featureEvaluator = featureEvaluator;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

}
