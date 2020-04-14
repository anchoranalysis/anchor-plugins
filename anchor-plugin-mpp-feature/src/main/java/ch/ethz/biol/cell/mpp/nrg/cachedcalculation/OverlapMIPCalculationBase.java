package ch.ethz.biol.cell.mpp.nrg.cachedcalculation;

import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMap;
import org.anchoranalysis.anchor.mpp.feature.nrg.elem.NRGElemPairCalcParams;
import org.anchoranalysis.anchor.mpp.overlap.MaxIntensityProjectionPair;
import org.anchoranalysis.anchor.mpp.pxlmark.PxlMark;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.PxlMarkMemo;
import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.feature.cachedcalculation.CachedCalculationCastParams;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public abstract class OverlapMIPCalculationBase extends CachedCalculationCastParams<Double, NRGElemPairCalcParams> {

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
		
		RegionMap regionMap1 = params.getObj1().getRegionMap();
		RegionMap regionMap2 = params.getObj2().getRegionMap();
		
		PxlMark pm1 = mark1.doOperation();
		PxlMark pm2 = mark2.doOperation();
		
		if (!pm1.getBoundingBoxMIP(regionID).hasIntersection(pm2.getBoundingBoxMIP(regionID))) {
			return 0.0;
		}
		
		MaxIntensityProjectionPair pair =
			new MaxIntensityProjectionPair(
				pm1.getObjMaskMIP().getVoxelBoxBounded(),
				pm2.getObjMaskMIP().getVoxelBoxBounded(),
				regionMap1.membershipWithFlagsForIndex(regionID),
				regionMap2.membershipWithFlagsForIndex(regionID)
			);
		
		double overlap = pair.countIntersectingPixels();
		
		return calculateOverlapResult(overlap, pair);
	}
		
	@Override
	public int hashCode() {
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
}
