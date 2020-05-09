package org.anchoranalysis.plugin.operator.feature.bean.arithmetic;

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
import org.anchoranalysis.feature.bean.operator.FeatureGenericSingleElem;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;


/**
 * Finds the repciprocal (multiplicate inverse), but imposes a maximum ceiling via a constant.
 * 
 * @author Owen Feehan
 *
 * @param <T> feature input-type
 */
public class InvertedMax<T extends FeatureInput> extends FeatureGenericSingleElem<T> {

	// START BEAN PARAMETERS
	@BeanField
	private double max = 100;
	// END BEAN PARAMETERS
	
	@Override
	public double calc( SessionInput<T> input ) throws FeatureCalcException {
		return Math.min(
			(1/ input.calc( getItem() )),
			max
		);
	}
	
	@Override
	public String getParamDscr() {
		return String.format("inverted_max=%f", max);
	}
	
	@Override
	public String getDscrLong() {
		return "(1/" + getItem().getBeanName() + ")[max=" + max + "]";
	}

	public double getMax() {
		return max;
	}

	public void setMax(double max) {
		this.max = max;
	}
}
