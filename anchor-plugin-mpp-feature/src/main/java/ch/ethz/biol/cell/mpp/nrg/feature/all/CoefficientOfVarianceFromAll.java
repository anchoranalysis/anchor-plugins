package ch.ethz.biol.cell.mpp.nrg.feature.all;

import org.anchoranalysis.anchor.mpp.feature.bean.nrg.elem.NRGElemAll;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputAllMemo;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputSingleMemo;
import org.anchoranalysis.anchor.mpp.feature.mark.MemoCollection;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.PxlMarkMemo;

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
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.CacheableParams;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.params.FeatureInput;

public class CoefficientOfVarianceFromAll extends NRGElemAll {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private Feature<FeatureInput> item;
	// END BEAN PROPERTIES
	
	@Override
	public double calc(CacheableParams<FeatureInputAllMemo> paramsCacheable)
			throws FeatureCalcException {
		
		MemoCollection memoMarks = paramsCacheable.getParams().getPxlPartMemo();
		
		if (memoMarks.size()==0) {
			return 0.0;
		}
		
		double vals[] = new double[memoMarks.size()];
		double mean = calcForEachItem(paramsCacheable, memoMarks, vals);
		
		if (mean==0.0) {
			return Double.POSITIVE_INFINITY;
		}
		
		return stdDev(vals, mean) / mean;
	}
	
	/** Calculates the feature on each mark separately, populating vals, and returns the mean */
	private double calcForEachItem( CacheableParams<FeatureInputAllMemo> paramsCacheable, MemoCollection memoMarks, double vals[] ) throws FeatureCalcException {
		
		double sum = 0.0;		
		
		for( int i=0; i<memoMarks.size(); i++) {
			
			final int index = i;
			
			double v = paramsCacheable.calcChangeParams(
				item,
				p -> extractInd(p, index),
				"mark"+i
			);
			
			vals[i] = v;
			sum+= v;
		}
		
		return sum / memoMarks.size();
	}
	
	private static FeatureInputSingleMemo extractInd( FeatureInputAllMemo params, int i ) {
		
		PxlMarkMemo pmm = params.getPxlPartMemo().getMemoForIndex(i);
		
		FeatureInputSingleMemo paramsInd = new FeatureInputSingleMemo(null,params.getNrgStack());
		paramsInd.setPxlPartMemo(pmm);
		return paramsInd;
	}
	
	private static double stdDev( double vals[], double mean ) {
		double sumSqDiff = 0.0;
		for( int i=0; i<vals.length; i++) {
			double diff = vals[i] - mean;
			sumSqDiff += Math.pow(diff,2.0);
		}
		
		return Math.sqrt(sumSqDiff);
	}

	public Feature<FeatureInput> getItem() {
		return item;
	}

	public void setItem(Feature<FeatureInput> item) {
		this.item = item;
	}


}
