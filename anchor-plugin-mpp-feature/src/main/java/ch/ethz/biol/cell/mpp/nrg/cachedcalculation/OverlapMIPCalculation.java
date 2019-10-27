package ch.ethz.biol.cell.mpp.nrg.cachedcalculation;

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
import org.anchoranalysis.feature.cachedcalculation.CachedCalculationCastParams;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import ch.ethz.biol.cell.mpp.mark.overlap.OverlapUtilities;
import ch.ethz.biol.cell.mpp.mark.pxlmark.PxlMark;
import ch.ethz.biol.cell.mpp.mark.pxlmark.memo.PxlMarkMemo;
import ch.ethz.biol.cell.mpp.mark.regionmap.RegionMap;
import ch.ethz.biol.cell.mpp.nrg.NRGElemPairCalcParams;

public class OverlapMIPCalculation extends CachedCalculationCastParams<Double, NRGElemPairCalcParams> {

	private int regionID;
	
	// Constructor
	public OverlapMIPCalculation( int regionID ) {
		super();
		this.regionID = regionID;
	}
	
	@Override
	protected Double execute( NRGElemPairCalcParams params ) throws ExecuteException {
		
		PxlMarkMemo mark1 = params.getObj1();
		PxlMarkMemo mark2 = params.getObj2();
		
		assert( mark1 != null );
		assert( mark2 != null );
		
		RegionMap regionMap1 = params.getObj1().getRegionMap();
		RegionMap regionMap2 = params.getObj2().getRegionMap();
				
		PxlMark pm1 = mark1.doOperation();
		PxlMark pm2 = mark2.doOperation();
		
		if (!pm1.getBoundingBoxMIP(regionID).hasIntersection(pm2.getBoundingBoxMIP(regionID))) {
			return 0.0;
		}
		
		OverlapUtilities.MaxIntensityProjectionPair mipPair =
			new OverlapUtilities.MaxIntensityProjectionPair(
				pm1.getObjMaskMIP().getVoxelBoxBounded(),
				pm2.getObjMaskMIP().getVoxelBoxBounded(),
				regionMap1.membershipWithFlagsForIndex(regionID),
				regionMap2.membershipWithFlagsForIndex(regionID)
			);
		int overlap = mipPair.countIntersectingPixels();

		return (double) overlap;
	}
	
	@Override
	public CachedCalculation<Double> duplicate() {
		return new OverlapMIPCalculation( regionID );
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(regionID).toHashCode();
	}
	
	@Override
	public boolean equals(final Object obj){
	    if(obj instanceof OverlapMIPCalculation){
	        final OverlapMIPCalculation other = (OverlapMIPCalculation) obj;
	        return new EqualsBuilder()
	            .append(regionID, other.regionID)
	            .isEquals();
	    } else{
	        return false;
	    }
	}
}
