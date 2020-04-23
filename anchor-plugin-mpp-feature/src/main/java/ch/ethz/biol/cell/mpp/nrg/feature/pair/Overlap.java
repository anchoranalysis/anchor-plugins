package ch.ethz.biol.cell.mpp.nrg.feature.pair;

import org.anchoranalysis.anchor.mpp.feature.bean.nrg.elem.FeaturePairMemo;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputPairMemo;
import org.anchoranalysis.anchor.mpp.mark.GlobalRegionIdentifiers;

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
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.cache.calculation.CachedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import ch.ethz.biol.cell.mpp.nrg.cachedcalculation.OverlapCalculation;
import ch.ethz.biol.cell.mpp.nrg.cachedcalculation.OverlapMIPCalculation;

public class Overlap extends FeaturePairMemo {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private int regionID = GlobalRegionIdentifiers.SUBMARK_INSIDE;
	
	@BeanField
	private boolean mip = false;
	// END BEAN PROPERTIES
	
	public Overlap() {
	}
		
	@Override
	public double calc( SessionInput<FeatureInputPairMemo> params ) throws FeatureCalcException {
		return params.calc( overlapCalculation() );
	}
	
	private CachedCalculation<Double,FeatureInputPairMemo> overlapCalculation() {
		if (mip) {
			return new OverlapMIPCalculation(regionID);
		} else {
			return new OverlapCalculation(regionID);
		}
	}
	
	public int getRegionID() {
		return regionID;
	}

	public void setRegionID(int regionID) {
		this.regionID = regionID;
	}

	public boolean isMip() {
		return mip;
	}

	public void setMip(boolean mip) {
		this.mip = mip;
	}


}
