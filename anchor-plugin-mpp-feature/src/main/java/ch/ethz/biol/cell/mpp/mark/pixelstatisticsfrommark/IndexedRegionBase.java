package ch.ethz.biol.cell.mpp.mark.pixelstatisticsfrommark;

import org.anchoranalysis.anchor.mpp.mark.GlobalRegionIdentifiers;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.pxlmark.PxlMark;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.PxlMarkMemo;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;

/**
 * {@link PixelStatisticsFromMark} with a region and an index
 * 
 * @author owen
 *
 */
public abstract class IndexedRegionBase extends PixelStatisticsFromMark {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private int index = 0;
	
	@BeanField
	private int regionID = GlobalRegionIdentifiers.SUBMARK_INSIDE;
	// END BEAN PROPERTIES
	
	@Override
	public String toString() {
		return String.format("regionID=%d,index=%d", regionID, index);
	}
	
	@Override
	public VoxelStatistics createStatisticsFor(PxlMarkMemo pmm, ImageDim dim) throws CreateException {
		
		PxlMark pm;
		try {
			pm = pmm.doOperation();
		} catch (ExecuteException e) {
			throw new CreateException(e);
		}
		
		return createStatisticsFor(pm, pmm.getMark(), dim);
	}
	
	protected abstract VoxelStatistics createStatisticsFor(PxlMark pm, Mark mark, ImageDim dim) throws CreateException;
	
	protected VoxelStatistics sliceStatisticsForRegion(PxlMark pm, int z) {
		return pm.statisticsFor(index, regionID, z);
	}
	
	protected BoundingBox boundingBoxForRegion( PxlMark pm ) {
		return pm.getBoundingBox(regionID);
	}
			
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getRegionID() {
		return regionID;
	}

	public void setRegionID(int regionID) {
		this.regionID = regionID;
	}
}