package ch.ethz.biol.cell.mpp.nrg.feature.operator;

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
import org.anchoranalysis.feature.bean.operator.FeatureGenericSingleElem;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.params.FeatureInput;


// A score between 0 and 1, based upon the CDF of a guassian. as one approaches the mean, the score approaches 1.0
public class LinearScore<T extends FeatureInput> extends FeatureGenericSingleElem<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private Feature<T> itemMin = null;
	
	@BeanField
	private Feature<T> itemMax = null;
	
	@BeanField
	private double minValue = 0.0;		// What the minimum value is set to, normally 0
	// END BEAN PROPERTIES
	
	public static double calc( double val, double min, double max ) {
		// Values higher than the mean should be included for definite
		if (val>max) {
			return 1.0;
		}
		
		double dist = (max-min+1);
		
		return 1.0 - ((max-val)/dist);
	}
	
	
	@Override
	public double calc( SessionInput<T> input ) throws FeatureCalcException {
		
		double val = input.calc( getItem() );
		
		double min = input.calc( getItemMin() );
		double max = input.calc( getItemMax() );
	
		if (minValue!=0.0) {
			// We rescale the minvalue so that our old min value lies at minValue instead of 0.0			
			double rangeOld = max-min;
			double rangeNew = rangeOld /(1-minValue); 
			min = max - rangeNew;
		}
		
		return calc( val, min, max );
	}
	
	@Override
	public String getDscrLong() {
		return String.format("pdf(%s,%s,%s)", getItem().getDscrLong(), getItemMin().getDscrLong(), getItemMax().getDscrLong() );
	}
	
	public Feature<T> getItemMin() {
		return itemMin;
	}

	public void setItemMin(Feature<T> itemMin) {
		this.itemMin = itemMin;
	}

	public Feature<T> getItemMax() {
		return itemMax;
	}

	public void setItemMax(Feature<T> itemMax) {
		this.itemMax = itemMax;
	}

	public double getMinValue() {
		return minValue;
	}

	public void setMinValue(double minValue) {
		this.minValue = minValue;
	}
	


}
