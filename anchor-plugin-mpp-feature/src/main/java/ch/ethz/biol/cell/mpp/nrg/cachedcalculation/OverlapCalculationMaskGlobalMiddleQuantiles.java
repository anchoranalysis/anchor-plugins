package ch.ethz.biol.cell.mpp.nrg.cachedcalculation;

import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputPairMemo;
import org.anchoranalysis.anchor.mpp.overlap.OverlapUtilities;


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


import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.feature.cache.calculation.CacheableCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.chnl.Chnl;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class OverlapCalculationMaskGlobalMiddleQuantiles extends CacheableCalculation<Double,FeatureInputPairMemo> {

	private int regionID;
	private int nrgIndex;
	private byte maskOnValue;
	
	private double quantileLower;
	private double quantileHigher;
	
	// Constructor
	public OverlapCalculationMaskGlobalMiddleQuantiles( int regionID, int nrgIndex, byte maskOnValue, double quantileLower, double quantileHigher ) {
		super();
		this.regionID = regionID;
		this.nrgIndex = nrgIndex;
		this.maskOnValue = maskOnValue;
		this.quantileLower = quantileLower;
		this.quantileHigher = quantileHigher;
	}

	@Override
	protected Double execute( FeatureInputPairMemo input ) throws ExecuteException {
		
		try {
			Chnl chnl = input.getNrgStackRequired().getNrgStack().getChnl(nrgIndex);
			
			return OverlapUtilities.overlapWithMaskGlobalMiddleRange(
				input.getObj1(),
				input.getObj2(),
				regionID,
				chnl.getVoxelBox().asByte(),
				maskOnValue,
				quantileLower,
				quantileHigher
			);
		} catch (FeatureCalcException e) {
			throw new ExecuteException(e);
		}
	}
	
	@Override
	public boolean equals(final Object obj){
	    if(obj instanceof OverlapCalculationMaskGlobalMiddleQuantiles){
	        final OverlapCalculationMaskGlobalMiddleQuantiles other = (OverlapCalculationMaskGlobalMiddleQuantiles) obj;
	        return new EqualsBuilder()
	            .append(regionID, other.regionID)
	            .append(nrgIndex, other.nrgIndex)
	            .append(maskOnValue, other.maskOnValue)
	            .append(quantileLower, other.quantileLower)
	            .append(quantileHigher, other.quantileHigher)
	            .isEquals();
	    } else{
	        return false;
	    }
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(regionID).append(nrgIndex).append(maskOnValue).append(quantileLower).append(quantileHigher).toHashCode();
	}
}
