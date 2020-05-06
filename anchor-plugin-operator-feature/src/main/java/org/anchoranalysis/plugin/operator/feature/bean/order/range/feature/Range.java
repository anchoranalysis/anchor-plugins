package org.anchoranalysis.plugin.operator.feature.bean.order.range.feature;

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
import org.anchoranalysis.feature.bean.operator.FeatureDoubleElem;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;


/**
 * The statistical range between two values i.e. the max-value minus the min-value.
 * 
 * <p>Optionally two different weights can be applied, depending on whether the first or second value is the higher one.</p>
 * 
 * @author Owen Feehan
 *
 * @param <T>
 */
public class Range<T extends FeatureInput> extends FeatureDoubleElem<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private double weightItem1Greater = 1.0;
	
	@BeanField
	private double weightItem2Greater = 1.0;
	// END BEAN PROPERTIES
	
	@Override
	public double calc( SessionInput<T> input ) throws FeatureCalcException {
		
		double val1 = input.calc( getItem1() );
		double val2 = input.calc( getItem2() );
		
		if (val1==val2) {
			return 0.0;
		}
		else if (val1>val2) {
			return weightItem1Greater * (val1-val2);
		} else {
			return weightItem2Greater * (val2-val1);
		}
	}

	@Override
	public String getDscrLong() {
		return String.format("%s - %s", getItem1().getDscrLong(), getItem2().getDscrLong() );
	}
	
	public double getWeightItem1Greater() {
		return weightItem1Greater;
	}

	public void setWeightItem1Greater(double weightItem1Greater) {
		this.weightItem1Greater = weightItem1Greater;
	}

	public double getWeightItem2Greater() {
		return weightItem2Greater;
	}

	public void setWeightItem2Greater(double weightItem2Greater) {
		this.weightItem2Greater = weightItem2Greater;
	}
}
