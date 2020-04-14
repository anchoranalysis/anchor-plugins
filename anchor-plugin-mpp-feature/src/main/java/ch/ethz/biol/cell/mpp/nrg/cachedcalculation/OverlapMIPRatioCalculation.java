package ch.ethz.biol.cell.mpp.nrg.cachedcalculation;

import org.anchoranalysis.anchor.mpp.overlap.MaxIntensityProjectionPair;


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


import org.anchoranalysis.feature.cachedcalculation.CachedCalculation;

public class OverlapMIPRatioCalculation extends OverlapMIPCalculationBase {
	
	// Constructor
	public OverlapMIPRatioCalculation( int regionID ) {
		super(regionID);
	}

	@Override
	protected Double calculateOverlapResult(double overlap, MaxIntensityProjectionPair pair) {
		if (overlap==0) {
			return 0.0;
		}
		
		int minArea = pair.minArea();
		return overlap / minArea;
	}
	
	@Override
	public CachedCalculation<Double> duplicate() {
		return new OverlapMIPRatioCalculation( getRegionID() );
	}
	
	@Override
	public boolean equals(final Object obj){
	    if(obj instanceof OverlapMIPRatioCalculation){
	        return isRegionIDEqual( (OverlapMIPRatioCalculation) obj );
	    } else{
	        return false;
	    }
	}
	
	@Override
	public int hashCode() {
		return regionIDHashCode();
	}
}
