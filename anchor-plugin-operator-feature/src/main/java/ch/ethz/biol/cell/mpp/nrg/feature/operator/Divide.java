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
import org.anchoranalysis.feature.bean.operator.FeatureListElem;
import org.anchoranalysis.feature.cache.CacheableParams;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.params.FeatureCalcParams;


public class Divide extends FeatureListElem {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private boolean avoidDivideByZero = false;
	
	@BeanField
	private double divideByZeroValue = 1e+15;
	// END BEAN PROPERTIES
	
	@Override
	public double calc( CacheableParams<? extends FeatureCalcParams> params ) throws FeatureCalcException {
		
		int size = getList().size();
		
		double result = params.calc( getList().get(0) );
		
		for (int i=1; i<size; i++) {
			double div = params.calc( getList().get(i) );
			if (div==0.0) {
				if (avoidDivideByZero) {
					return divideByZeroValue;
				} else {
					throw new FeatureCalcException( String.format("Divide by zero from feature %s", getList().get(i).getFriendlyName() ) );
				}
			}
			result /= div;
		}
		return result;
	}
	
	@Override
	public String getDscrLong() {
		
		StringBuilder sb = new StringBuilder();
		
		boolean first = true;
		for (Feature elem : getList()) {
			
			if (first==true) {
				first = false;
			} else {
				sb.append("/");
			}
		
			sb.append(elem.getDscrLong());
		}
				
		return sb.toString();
	}
	
	public boolean isAvoidDivideByZero() {
		return avoidDivideByZero;
	}

	public void setAvoidDivideByZero(boolean avoidDivideByZero) {
		this.avoidDivideByZero = avoidDivideByZero;
	}

	public double getDivideByZeroValue() {
		return divideByZeroValue;
	}

	public void setDivideByZeroValue(double divideByZeroValue) {
		this.divideByZeroValue = divideByZeroValue;
	}
}
