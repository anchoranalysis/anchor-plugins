package ch.ethz.biol.cell.mpp.nrg.cachedcalculation;

import org.anchoranalysis.anchor.mpp.feature.nrg.elem.NRGElemPairCalcParams;
import org.anchoranalysis.anchor.mpp.overlap.OverlapUtilities;
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


import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.feature.cachedcalculation.CachedCalculation;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.chnl.Chnl;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class OverlapCalculationMaskGlobal extends CachedCalculation<Double,NRGElemPairCalcParams> {

	private int regionID;
	private int nrgIndex;
	private byte maskOnValue;
		
	// Constructor
	public OverlapCalculationMaskGlobal( int regionID, int nrgIndex, byte maskOnValue ) {
		super();
		this.regionID = regionID;
		this.nrgIndex = nrgIndex;
		this.maskOnValue = maskOnValue;
	}

	@Override
	protected Double execute( NRGElemPairCalcParams params ) throws ExecuteException {
		
		PxlMarkMemo mark1 = params.getObj1();
		PxlMarkMemo mark2 = params.getObj2();
		
		assert( mark1 != null );
		assert( mark2 != null );
		
		NRGStackWithParams nrgStack = params.getNrgStack();
		Chnl chnl = nrgStack.getNrgStack().getChnl(nrgIndex);
		
		return OverlapUtilities.overlapWithMaskGlobal(
			mark1,
			mark2,
			regionID,
			chnl.getVoxelBox().asByte(),
			maskOnValue
		);
	}
	
	@Override
	public boolean equals(final Object obj){
	    if(obj instanceof OverlapCalculationMaskGlobal){
	        final OverlapCalculationMaskGlobal other = (OverlapCalculationMaskGlobal) obj;
	        return new EqualsBuilder()
	            .append(regionID, other.regionID)
	            .append(nrgIndex, other.nrgIndex)
	            .append(maskOnValue, other.maskOnValue)
	            .isEquals();
	    } else{
	        return false;
	    }
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(regionID).append(nrgIndex).append(maskOnValue).toHashCode();
	}
}
