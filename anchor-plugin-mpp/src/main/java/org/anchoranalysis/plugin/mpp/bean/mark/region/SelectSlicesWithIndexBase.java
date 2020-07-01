package org.anchoranalysis.plugin.mpp.bean.mark.region;

import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.pxlmark.PxlMark;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;

public abstract class SelectSlicesWithIndexBase extends SelectSlicesBase {

	// START BEAN PROPERTIES
	@BeanField
	private int indexNonZero = 0;
	// END BEAN PROPERTIES
	
	@Override
	protected VoxelStatistics createStatisticsFor(PxlMark pm, Mark mark, ImageDimensions dim) throws CreateException {
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
	
	public int getIndexNonZero() {
		return indexNonZero;
	}

	public void setIndexNonZero(int indexNonZero) {
		this.indexNonZero = indexNonZero;
	}
	
	@Override
	public String uniqueName() {
		return String.format(
			"%s_%d",
			super.uniqueName(),
			indexNonZero
		);
	}
	
	@Override
	public String toString() {
		return String.format("regionID=%d,index=%d,indexNonZero=%d", getRegionID(), getIndex(), indexNonZero);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + indexNonZero;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SelectSlicesWithIndexBase other = (SelectSlicesWithIndexBase) obj;
		if (indexNonZero != other.indexNonZero)
			return false;
		return true;
	}
}
