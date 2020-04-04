package ch.ethz.biol.cell.mpp.nrg.feature.pair;

import org.anchoranalysis.anchor.mpp.feature.bean.nrg.elem.NRGElemPair;
import org.anchoranalysis.anchor.mpp.feature.nrg.elem.NRGElemIndCalcParams;
import org.anchoranalysis.anchor.mpp.feature.nrg.elem.NRGElemPairCalcParams;

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
public class FeatureAsIndividualProportionate extends NRGElemPair {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private Feature item;
	
	@BeanField
	private Feature itemProportionate;
	// eND BEAN PROPERTIES
	
	public FeatureAsIndividualProportionate() {
	}

	@Override
	public double calcCast( CacheableParams<NRGElemPairCalcParams> params ) throws FeatureCalcException {
		
		CacheableParams<NRGElemIndCalcParams> params1 = params.changeParams(
			new NRGElemIndCalcParams( params.getParams().getObj1(), params.getParams().getNrgStack() )
		);
		CacheableParams<NRGElemIndCalcParams> params2 = params.changeParams(
			new NRGElemIndCalcParams( params.getParams().getObj2(), params.getParams().getNrgStack() )
		);
		
		double val1 = getCacheSession().calc( item, params1 );
		double val2 = getCacheSession().calc( item, params2 );
		
		double prop1 = getCacheSession().calc( itemProportionate, params1 );
		double prop2 = getCacheSession().calc( itemProportionate, params2 );
		
		// Normalise
		double propSum = prop1 + prop2;
		prop1 /= propSum;
		prop2 /= propSum;
		
		return (prop1*val1) + (prop2*val2);
	}

	public Feature getItem() {
		return item;
	}

	public void setItem(Feature item) {
		this.item = item;
	}

	public Feature getItemProportionate() {
		return itemProportionate;
	}

	public void setItemProportionate(Feature itemProportionate) {
		this.itemProportionate = itemProportionate;
	}

}
