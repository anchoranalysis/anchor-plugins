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
import org.anchoranalysis.feature.bean.operator.FeatureDoubleElem;
import org.anchoranalysis.feature.cache.CacheableParams;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.params.FeatureCalcParams;


public class DivideExplicit<T extends FeatureCalcParams> extends FeatureDoubleElem<T> {
	
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
	public double calc( CacheableParams<T> params ) throws FeatureCalcException {
		
		// This feature doesn't need to be init, so we skip this bit
		double val1 = params.calc( getItem1() );
		double val2 = params.calc( getItem2() );
		
		//getLogger().getLogReporter().logFormatted("Divide Explicit: %f on %f = %f", val1, val2, val1/val2);
		
		if (avoidDivideByZero && val2==0.0) {
			return divideByZeroValue;
		}
		
		return val1/val2;
	}
	
	@Override
	public String getDscrLong() {
		return String.format("%s/%s", getItem1().getDscrLong(), getItem2().getDscrLong());
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
