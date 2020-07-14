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
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.feature.bean.evaluator.FeatureEvaluator;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.obj.filter.ObjectFilterPredicate;

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
	protected boolean precondition(ObjectCollection objectsToFilter) {
		// We abandon the filtering if we have too small a number of objects, as statistics won't be meaningful
		return objectsToFilter.size()>=minNumObjs;
	}

	@Override
	protected void start(Optional<ImageDimensions> dim, ObjectCollection objectsToFilter) throws OperationFailedException {
		super.start(dim, objectsToFilter);
		
		// Now we calculate feature values for each object, and a standard deviation
		featureVals = calcFeatures(objectsToFilter, featureEvaluator.createAndStartSession() );
		
		featureMap = createFeatureMap(objectsToFilter, featureVals);
		
		double quantileVal = calcQuantile(featureVals);
		minVal = quantileVal * minRatio;
		
		if (getLogger()!=null) {
			getLogger().messageLogger().log("START DiscardOutliers");
			getLogger().messageLogger().logFormatted("quantileVal(%f)=%f   minVal=%f", quantile,quantileVal, minVal);
		}
	}

	@Override
	protected boolean match(ObjectMask object, Optional<ImageDimensions> dim) throws OperationFailedException {

		double featureVal = featureMap.get(object);
		boolean matched = featureVal>=minVal; 
		
		if (!matched && getLogger()!=null) {
			getLogger().messageLogger().logFormatted("discard with val=%f", featureVal);
		}
		
		return matched;
	}

	@Override
	protected void end() throws OperationFailedException {
		super.end();
		featureVals = null;
		if (getLogger()!=null) {
			getLogger().messageLogger().log("END DiscardOutliers");
		}
	}	

	private static DoubleArrayList calcFeatures( ObjectCollection objects, FeatureCalculatorSingle<FeatureInputSingleObject> calculator ) throws OperationFailedException {
		DoubleArrayList featureVals = new DoubleArrayList();
		for( ObjectMask objectMask : objects ) {
			try {
				featureVals.add(
					calculator.calc(
						new FeatureInputSingleObject(objectMask)
					)
				);
			} catch (FeatureCalcException e) {
				throw new OperationFailedException(e);
			}
		}
		return featureVals;
	}
	
	private static Map<ObjectMask,Double> createFeatureMap(ObjectCollection objectsToFilter, DoubleArrayList featureVals) {
		assert(objectsToFilter.size()==featureVals.size());
		
		Map<ObjectMask,Double> map = new HashMap<>();
			
		for(int i=0; i<objectsToFilter.size(); i++) {
			map.put(
				objectsToFilter.get(i),
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
