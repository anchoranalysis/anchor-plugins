package org.anchoranalysis.plugin.image.bean.obj.filter.dependent;

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


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.feature.bean.evaluator.FeatureEvaluator;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.objectmask.ObjectMask;
import org.anchoranalysis.plugin.image.bean.obj.filter.ObjectFilterPredicate;
import org.anchoranalysis.image.objectmask.ObjectCollection;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;

/**
 * Calculates features values for all objects, and discards any object less than <code>quantile - (minRatio * stdDev)</code>
 * 
 * @author Owen Feehan
 *
 */
public class DiscardOutliers extends ObjectFilterPredicate {

	// START BEAN PROPERTIES
	@BeanField
	private FeatureEvaluator<FeatureInputSingleObject> featureEvaluator;
	
	@BeanField
	private double quantile;
	
	@BeanField
	private double minRatio;
	
	@BeanField
	private int minNumObjs = 1;
	// END BEAN PROPERTIES
	
	private double minVal;
	private DoubleArrayList featureVals;
	
	/** A map between each object and its feature-value (inefficient as it introduces a map lookup per object) */
	private Map<ObjectMask,Double> featureMap;
	
	@Override
	protected boolean precondition(ObjectCollection objsToFilter) {
		// We abandon the filtering if we have too small a number of objects, as statistics won't be meaningful
		return objsToFilter.size()>=minNumObjs;
	}

	@Override
	protected void start(Optional<ImageDim> dim, ObjectCollection objsToFilter) throws OperationFailedException {
		super.start(dim, objsToFilter);
		
		// Now we calculate feature values for each object, and a standard deviation
		featureVals = calcFeatures(objsToFilter, featureEvaluator.createAndStartSession() );
		
		featureMap = createFeatureMap(objsToFilter, featureVals);
		
		double quantileVal = calcQuantile(featureVals);
		minVal = quantileVal * minRatio;
		
		if (getLogger()!=null) {
			getLogger().getLogReporter().log("START DiscardOutliers");
			getLogger().getLogReporter().logFormatted("quantileVal(%f)=%f   minVal=%f", quantile,quantileVal, minVal);
		}
	}

	@Override
	protected boolean match(ObjectMask om, Optional<ImageDim> dim) throws OperationFailedException {

		double featureVal = featureMap.get(om);
		boolean matched = featureVal>=minVal; 
		
		if (!matched && getLogger()!=null) {
			getLogger().getLogReporter().logFormatted("discard with val=%f", featureVal);
		}
		
		return matched;
	}

	@Override
	protected void end() throws OperationFailedException {
		super.end();
		featureVals = null;
		if (getLogger()!=null) {
			getLogger().getLogReporter().log("END DiscardOutliers");
		}
	}	

	private static DoubleArrayList calcFeatures( ObjectCollection objs, FeatureCalculatorSingle<FeatureInputSingleObject> calculator ) throws OperationFailedException {
		DoubleArrayList featureVals = new DoubleArrayList();
		for( ObjectMask om : objs ) {
			try {
				featureVals.add(
					calculator.calc(
						new FeatureInputSingleObject(om)
					)
				);
			} catch (FeatureCalcException e) {
				throw new OperationFailedException(e);
			}
		}
		return featureVals;
	}
	
	private static Map<ObjectMask,Double> createFeatureMap(ObjectCollection objsToFilter, DoubleArrayList featureVals) {
		assert(objsToFilter.size()==featureVals.size());
		
		Map<ObjectMask,Double> map = new HashMap<>();
			
		for(int i=0; i<objsToFilter.size(); i++) {
			map.put(
				objsToFilter.get(i),
				featureVals.get(i)
			);
		}
		return map;
	}
	
	private double calcQuantile( DoubleArrayList featureVals) {
		DoubleArrayList featureValsSorted = featureVals.copy();
		featureValsSorted.sort();
		return Descriptive.quantile(featureValsSorted,quantile);
	}
	
	public int getMinNumObjs() {
		return minNumObjs;
	}

	public void setMinNumObjs(int minNumObjs) {
		this.minNumObjs = minNumObjs;
	}

	public double getQuantile() {
		return quantile;
	}

	public void setQuantile(double quantile) {
		this.quantile = quantile;
	}

	public double getMinRatio() {
		return minRatio;
	}

	public void setMinRatio(double minRatio) {
		this.minRatio = minRatio;
	}

	public FeatureEvaluator<FeatureInputSingleObject> getFeatureEvaluator() {
		return featureEvaluator;
	}

	public void setFeatureEvaluator(FeatureEvaluator<FeatureInputSingleObject> featureEvaluator) {
		this.featureEvaluator = featureEvaluator;
	}
}
