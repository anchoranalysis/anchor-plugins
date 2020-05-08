package ch.ethz.biol.cell.mpp.nrg.feature.resultsvectorcollection;



/*
 * #%L
 * anchor-plugin-mpp-feature
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
import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;

public class Quantile extends FeatureResultsFromIndex {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private double quantile = 0;
	
	/**
	 * If true, the quantile is interpreted as a percentage rather than a decimal
	 */
	@BeanField
	private boolean asPercentage = false;
	// END BEAN PROPERTIES
	
	@Override
	protected double calcStatisticFromFeatureVal(DoubleArrayList featureVals) {
		featureVals.sort();
		
		double quantileFinal = asPercentage ? quantile/100 : quantile;
		return Descriptive.quantile(featureVals, quantileFinal);
	}
	
	public double getQuantile() {
		return quantile;
	}

	public void setQuantile(double quantile) {
		this.quantile = quantile;
	}

	public boolean isAsPercentage() {
		return asPercentage;
	}

	public void setAsPercentage(boolean asPercentage) {
		this.asPercentage = asPercentage;
	}
}
