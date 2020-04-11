package ch.ethz.biol.cell.mpp.nrg.feature.pair;

import org.anchoranalysis.anchor.mpp.feature.bean.nrg.elem.NRGElemPair;
import org.anchoranalysis.anchor.mpp.feature.nrg.elem.NRGElemPairCalcParams;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.PxlMarkMemo;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.core.relation.RelationToValue;
import org.anchoranalysis.feature.cache.CacheableParams;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;

import ch.ethz.biol.cell.mpp.nrg.cachedcalculation.OverlapCalculationMaskGlobal;

public abstract class OverlapMaskBase extends NRGElemPair {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private int maskValue = 255;
	
	@BeanField
	private int nrgIndex = 0;
	// END BEAN PROPERTIES
		
	protected double overlapForRegion( CacheableParams<NRGElemPairCalcParams> paramsCacheable, int regionID ) throws ExecuteException {
		return paramsCacheable.calc(
			new OverlapCalculationMaskGlobal(regionID, nrgIndex, (byte) maskValue)
		);
	}
	
	protected double calcMinVolume(
		PxlMarkMemo obj1,
		PxlMarkMemo obj2,
		int regionID,
		RelationToValue relationToThreshold
	) throws FeatureCalcException {
		try {
			VoxelStatistics pxlStats1 =  obj1.doOperation().statisticsForAllSlices(nrgIndex, regionID);
			VoxelStatistics pxlStats2 =  obj2.doOperation().statisticsForAllSlices(nrgIndex, regionID);
			
			long size1 = pxlStats1.countThreshold(relationToThreshold, maskValue);
			long size2 = pxlStats2.countThreshold(relationToThreshold, maskValue);
			return Math.min( size1, size2 );
		} catch (ExecuteException e) {
			throw new FeatureCalcException(e);
		}
	}
	
	protected double calcMaxVolume(
		PxlMarkMemo obj1,
		PxlMarkMemo obj2,
		int regionID,
		RelationToValue relationToThreshold
	) throws FeatureCalcException {
		try {
			VoxelStatistics pxlStats1 =  obj1.doOperation().statisticsForAllSlices(nrgIndex, regionID);
			VoxelStatistics pxlStats2 =  obj2.doOperation().statisticsForAllSlices(nrgIndex, regionID);
			
			long size1 = pxlStats1.countThreshold(relationToThreshold, maskValue);
			long size2 = pxlStats2.countThreshold(relationToThreshold, maskValue);
			return Math.max( size1, size2 );
		} catch (ExecuteException e) {
			throw new FeatureCalcException(e);
		}
	}
	
	public int getNrgIndex() {
		return nrgIndex;
	}

	public void setNrgIndex(int nrgIndex) {
		this.nrgIndex = nrgIndex;
	}

	public int getMaskValue() {
		return maskValue;
	}

	public void setMaskValue(int maskValue) {
		this.maskValue = maskValue;
	}
}
