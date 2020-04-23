package ch.ethz.biol.cell.mpp.nrg.cachedcalculation;

/*-
 * #%L
 * anchor-plugin-mpp-feature
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan
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

import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMembershipWithFlags;
import org.anchoranalysis.anchor.mpp.feature.nrg.elem.NRGElemPairCalcParams;
import org.anchoranalysis.anchor.mpp.overlap.MaxIntensityProjectionPair;
import org.anchoranalysis.anchor.mpp.pxlmark.PxlMark;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.PxlMarkMemo;
import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.feature.cache.calculation.CachedCalculation;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public abstract class OverlapMIPCalculationBase extends CachedCalculation<Double, NRGElemPairCalcParams> {

	private int regionID;

	// Constructor
	public OverlapMIPCalculationBase( int regionID ) {
		super();
		this.regionID = regionID;
	}

	@Override
	protected Double execute( NRGElemPairCalcParams params ) throws ExecuteException {
		
		PxlMarkMemo mark1 = params.getObj1();
		PxlMarkMemo mark2 = params.getObj2();
		
		assert( mark1 != null );
		assert( mark2 != null );
		
		PxlMark pm1 = mark1.doOperation();
		PxlMark pm2 = mark2.doOperation();
		
		if (!pm1.getBoundingBoxMIP(regionID).hasIntersection(pm2.getBoundingBoxMIP(regionID))) {
			return 0.0;
		}
		
		MaxIntensityProjectionPair pair =
			new MaxIntensityProjectionPair(
				pm1.getObjMaskMIP().getVoxelBoxBounded(),
				pm2.getObjMaskMIP().getVoxelBoxBounded(),
				regionMembershipForMark(mark1),
				regionMembershipForMark(mark2)
			);
		
		double overlap = pair.countIntersectingPixels();
		
		return calculateOverlapResult(overlap, pair);
	}
		
	protected int regionIDHashCode() {
		return new HashCodeBuilder().append(regionID).toHashCode();
	}
	
	protected abstract Double calculateOverlapResult( double overlap, MaxIntensityProjectionPair pair);
	
	protected boolean isRegionIDEqual(OverlapMIPCalculationBase other) {
		 return new EqualsBuilder()
            .append(regionID, other.regionID)
            .isEquals();
	}
	
	protected int getRegionID() {
		return regionID;
	}
		
	private RegionMembershipWithFlags regionMembershipForMark( PxlMarkMemo mark ) {
		return mark.getRegionMap().membershipWithFlagsForIndex(regionID);
	}
}
