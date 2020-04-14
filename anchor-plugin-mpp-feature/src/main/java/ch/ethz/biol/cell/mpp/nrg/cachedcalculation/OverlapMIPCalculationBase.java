package ch.ethz.biol.cell.mpp.nrg.cachedcalculation;

import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMembershipWithFlags;
import org.anchoranalysis.anchor.mpp.feature.nrg.elem.NRGElemPairCalcParams;
import org.anchoranalysis.anchor.mpp.overlap.MaxIntensityProjectionPair;
import org.anchoranalysis.anchor.mpp.pxlmark.PxlMark;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.PxlMarkMemo;
import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.feature.cachedcalculation.CachedCalculation;
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
