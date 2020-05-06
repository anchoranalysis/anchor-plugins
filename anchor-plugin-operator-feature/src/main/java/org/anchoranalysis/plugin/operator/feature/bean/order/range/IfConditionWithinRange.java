package org.anchoranalysis.plugin.operator.feature.bean.order.range;

/*
 * #%L
 * anchor-feature
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
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;


/**
 * Calculates a value if a condition lies within a certain range, otherwise returns constants
 * 
 * @author Owen Feehan
 *
 * @param <T> feature input type
 */
public class IfConditionWithinRange<T extends FeatureInput> extends RangeCompareFromScalars<T> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8939465493485894918L;
	
	// START BEAN PROPERTIES
	/** Calculates value for the condition - which is checked if it lies within a certain range */
	@BeanField
	private Feature<T> featureCondition;
	// END BEAN PROPERTIES
	
	@Override
	protected Feature<T> featureToCalcInputVal() {
		return featureCondition;
	}

	@Override
	protected double withinRangeValue(double valWithinRange, SessionInput<T> input) throws FeatureCalcException {
		// If the condition lies within the range, then we calculate the derived feature as intended
		return input.calc(
			getItem()
		);
	}
	
	public Feature<T> getFeatureCondition() {
		return featureCondition;
	}

	public void setFeatureCondition(Feature<T> featureCondition) {
		this.featureCondition = featureCondition;
	}
}