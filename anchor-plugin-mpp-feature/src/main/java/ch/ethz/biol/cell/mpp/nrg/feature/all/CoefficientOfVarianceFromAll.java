package ch.ethz.biol.cell.mpp.nrg.feature.all;

import org.anchoranalysis.anchor.mpp.feature.bean.nrg.elem.NRGElemAll;
import org.anchoranalysis.anchor.mpp.feature.nrg.elem.NRGElemAllCalcParams;
import org.anchoranalysis.anchor.mpp.feature.nrg.elem.NRGElemIndCalcParams;
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

public class CoefficientOfVarianceFromAll extends NRGElemAll {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private Feature item;
	// END BEAN PROPERTIES
	
	@Override
	public double calcCast(CacheableParams<NRGElemAllCalcParams> paramsCacheable)
			throws FeatureCalcException {
		
		double sum = 0.0;
		
		NRGElemAllCalcParams params = paramsCacheable.getParams();
		
		NRGElemIndCalcParams paramsInd = new NRGElemIndCalcParams(null,params.getNrgStack());
		
		if (params.getPxlPartMemo().size()==0) {
			return 0.0;
		}
		
		double vals[] = new double[params.getPxlPartMemo().size()];
		
		for( int i=0; i<params.getPxlPartMemo().size(); i++) {
			PxlMarkMemo pmm = params.getPxlPartMemo().getMemoForIndex(i);
			paramsInd.setPxlPartMemo(pmm);
			double v = getCacheSession().calc(
				item,
				paramsCacheable.changeParams(paramsInd)
			);
			vals[i] = v;
			sum+= v;
		}
		
		double mean = sum / params.getPxlPartMemo().size();
		
		double sumSqDiff = 0.0;
		for( int i=0; i<vals.length; i++) {
			double diff = vals[i] - mean;
			sumSqDiff += Math.pow(diff,2.0);
		}
		
		double stdDev = Math.sqrt(sumSqDiff);
		
		if (mean==0.0) {
			return Double.POSITIVE_INFINITY;
		}
		
		return stdDev / mean;
	}

	public Feature getItem() {
		return item;
	}

	public void setItem(Feature item) {
		this.item = item;
	}


}
