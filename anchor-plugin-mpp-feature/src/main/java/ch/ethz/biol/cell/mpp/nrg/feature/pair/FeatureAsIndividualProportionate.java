package ch.ethz.biol.cell.mpp.nrg.feature.pair;

import java.util.function.Function;

import org.anchoranalysis.anchor.mpp.feature.nrg.elem.NRGElemIndCalcParams;
import org.anchoranalysis.anchor.mpp.feature.nrg.elem.NRGElemPairCalcParams;
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

// Calculates each feature individually, and combines them using the ratios between itemProportionate
//   as weights
public class FeatureAsIndividualProportionate extends NRGElemPairWithFeature {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private Feature<NRGElemIndCalcParams> itemProportionate;
	// eND BEAN PROPERTIES
	
	public FeatureAsIndividualProportionate() {
	}

	@Override
	public double calcCast( CacheableParams<NRGElemPairCalcParams> params ) throws FeatureCalcException {
		
		CacheableParams<NRGElemIndCalcParams> params1 = deriveParams(params, p -> p.getObj1(), "obj1");
		CacheableParams<NRGElemIndCalcParams> params2 = deriveParams(params, p -> p.getObj2(), "obj2");

		return weightedSum(
			valueFor(params1),
			valueFor(params2),
			weightFor(params1),
			weightFor(params2)
		);
	}
	
	private double valueFor( CacheableParams<NRGElemIndCalcParams> params ) throws FeatureCalcException {
		return params.calc( getItem() );
	}
	
	private double weightFor( CacheableParams<NRGElemIndCalcParams> params ) throws FeatureCalcException {
		return params.calc(itemProportionate);
	}
	
	private static double weightedSum( double val1, double val2, double weight1, double weight2) {
		// Normalise
		double weightSum = weight1 + weight2;
		weight1 /= weightSum;
		weight2 /= weightSum;
		
		return (weight1*val1) + (weight2*val2);		
	}
	
	private static CacheableParams<NRGElemIndCalcParams> deriveParams( CacheableParams<NRGElemPairCalcParams> params, Function<NRGElemPairCalcParams,PxlMarkMemo> extractPmm, String cacheName ) {
		return params.mapParams(
			p -> extractInd(p, extractPmm),
			cacheName
		);
	}
	
	private static NRGElemIndCalcParams extractInd( NRGElemPairCalcParams p, Function<NRGElemPairCalcParams,PxlMarkMemo> extractPmm ) {
		return new NRGElemIndCalcParams(
			extractPmm.apply(p),
			p.getNrgStack()
		);
	}

	public Feature<NRGElemIndCalcParams> getItemProportionate() {
		return itemProportionate;
	}

	public void setItemProportionate(Feature<NRGElemIndCalcParams> itemProportionate) {
		this.itemProportionate = itemProportionate;
	}

}
