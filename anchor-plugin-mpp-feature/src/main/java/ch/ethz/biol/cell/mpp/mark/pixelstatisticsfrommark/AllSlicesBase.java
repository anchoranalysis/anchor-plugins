package ch.ethz.biol.cell.mpp.mark.pixelstatisticsfrommark;

import org.anchoranalysis.anchor.mpp.mark.GlobalRegionIdentifiers;
import org.anchoranalysis.anchor.mpp.pxlmark.PxlMark;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.PxlMarkMemo;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;

public abstract class AllSlicesBase extends PixelStatisticsFromMark {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private int index = 0;
	
	@BeanField
	private int indexNonZero = 0;
	
	@BeanField
	private int regionID = GlobalRegionIdentifiers.SUBMARK_INSIDE;
	
	@BeanField
	private int sliceID = -1;	// -1 indicates that we use all slices
	// END BEAN PROPERTIES
	
	@Override
	public VoxelStatistics createStatisticsFor(PxlMarkMemo pmm, ImageDim dim) throws CreateException {

		try {
			return extractFromPxlMark(
				pmm.doOperation()
			);
		} catch (ExecuteException e) {
			throw new CreateException(e.getCause());
		}
	}
	
	protected abstract VoxelStatistics extractFromPxlMark( PxlMark pm ) throws CreateException;

	protected VoxelStatistics statisticsForAllSlices( PxlMark pm, boolean useNonZeroIndex ) throws CreateException {
		return pm.statisticsForAllSlicesMaskSlice(
			useNonZeroIndex ? indexNonZero : index,
			regionID,
			indexNonZero
		);
	}
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
	@Override
	public String toString() {
		return String.format("regionID=%d,index=%d,indexNonZero=%d", regionID, index, indexNonZero);
	}

	public int getRegionID() {
		return regionID;
	}

	public void setRegionID(int regionID) {
		this.regionID = regionID;
	}

	public int getSliceID() {
		return sliceID;
	}

	public void setSliceID(int sliceID) {
		this.sliceID = sliceID;
	}

	public int getIndexNonZero() {
		return indexNonZero;
	}

	public void setIndexNonZero(int indexNonZero) {
		this.indexNonZero = indexNonZero;
	}
}
