package ch.ethz.biol.cell.mpp.nrg.feature.all;

import org.anchoranalysis.anchor.mpp.feature.bean.nrg.elem.FeatureAllMemo;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputAllMemo;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputSingleMemo;
import org.anchoranalysis.anchor.mpp.feature.mark.MemoCollection;


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
import org.anchoranalysis.feature.cache.ChildCacheName;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;

public class CoefficientOfVarianceFromAll extends FeatureAllMemo {

	// START BEAN PROPERTIES
	@BeanField
	private Feature<FeatureInputSingleMemo> item;
	// END BEAN PROPERTIES
		
	@Override
	public double calc(SessionInput<FeatureInputAllMemo> input)	throws FeatureCalcException {
		
		MemoCollection memoMarks = input.get().getPxlPartMemo();
		
		if (memoMarks.size()==0) {
			return 0.0;
		}
		
		return calcStatistic(input, memoMarks);
	}
	
	private double calcStatistic( SessionInput<FeatureInputAllMemo> input, MemoCollection memoMarks ) throws FeatureCalcException {
		
		double vals[] = new double[memoMarks.size()];
				
		double mean = calcForEachItem(input, memoMarks, vals);
		
		if (mean==0.0) {
			return Double.POSITIVE_INFINITY;
		}
		
		return stdDev(vals, mean) / mean;
	}
	
	/** Calculates the feature on each mark separately, populating vals, and returns the mean */
	private double calcForEachItem( SessionInput<FeatureInputAllMemo> input, MemoCollection memoMarks, double vals[] ) throws FeatureCalcException {
		
		double sum = 0.0;		
		
		for( int i=0; i<memoMarks.size(); i++) {
			
			final int index = i;
			
			double v = input.forChild().calc(
				item,
				new CalculateDeriveSingleMemoInput(index),
				new ChildCacheName(CoefficientOfVarianceFromAll.class, i)
			);
			
			vals[i] = v;
			sum+= v;
		}
		
		return sum / memoMarks.size();
	}
	
	private static double stdDev( double vals[], double mean ) {
		double sumSqDiff = 0.0;
		for( int i=0; i<vals.length; i++) {
			double diff = vals[i] - mean;
			sumSqDiff += Math.pow(diff,2.0);
		}
		
		return Math.sqrt(sumSqDiff);
	}

	public Feature<FeatureInputSingleMemo> getItem() {
		return item;
	}

	public void setItem(Feature<FeatureInputSingleMemo> item) {
		this.item = item;
	}
}
