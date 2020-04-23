package ch.ethz.biol.cell.mpp.nrg.feature.pair;

import java.util.function.Function;

import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputPairMemo;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputSingleMemo;
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


import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;

public class MinFeatureAsIndividual extends NRGElemPairWithFeature {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public double calc( SessionInput<FeatureInputPairMemo> params ) throws FeatureCalcException {

		return Math.min(
			calcForInd( params, p->p.getObj1(), "1" ),
			calcForInd( params, p->p.getObj2(), "2" )
		);
	}
	
	private double calcForInd(
		SessionInput<FeatureInputPairMemo> paramsCacheable,
		Function<FeatureInputPairMemo,PxlMarkMemo> pmmFunc,
		String suffix
	) throws FeatureCalcException {
		
		return paramsCacheable.calcChangeParams(
			getItem(),
			p -> deriveParams(p, pmmFunc),
			"pair_obj" + suffix
		);
	}
	
	private static FeatureInputSingleMemo deriveParams( FeatureInputPairMemo params, Function<FeatureInputPairMemo,PxlMarkMemo> pmmFunc ) {
		return new FeatureInputSingleMemo(
			pmmFunc.apply(params),
			params.getNrgStack()
		);
	}
}
