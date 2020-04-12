package ch.ethz.biol.cell.mpp.mark.pixelstatisticsfrommark;

import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.pxlmark.PxlMark;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;

public abstract class AllSlicesBase extends IndexedRegionBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private int indexNonZero = 0;
	
	@BeanField
	private int sliceID = -1;	// -1 indicates that we use all slices
	// END BEAN PROPERTIES

	@Override
	protected VoxelStatistics createStatisticsFor(PxlMark pm, Mark mark, ImageDim dim) throws CreateException {
		return extractFromPxlMark(pm);
	}
	
	protected abstract VoxelStatistics extractFromPxlMark( PxlMark pm ) throws CreateException;

	protected VoxelStatistics statisticsForAllSlices( PxlMark pm, boolean useNonZeroIndex ) throws CreateException {
		return pm.statisticsForAllSlicesMaskSlice(
			useNonZeroIndex ? indexNonZero : getIndex(),
			getRegionID(),
			indexNonZero
		);
	}
	
	@Override
	public String toString() {
		return String.format("regionID=%d,index=%d,indexNonZero=%d", getRegionID(), getIndex(), indexNonZero);
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
