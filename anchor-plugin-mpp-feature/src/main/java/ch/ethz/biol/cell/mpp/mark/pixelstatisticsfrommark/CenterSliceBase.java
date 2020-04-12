package ch.ethz.biol.cell.mpp.mark.pixelstatisticsfrommark;

import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.MarkAbstractPosition;
import org.anchoranalysis.anchor.mpp.pxlmark.PxlMark;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;

public abstract class CenterSliceBase extends IndexedRegionBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	protected VoxelStatistics createStatisticsFor(PxlMark pm, Mark mark, ImageDim dim) throws CreateException {
		
		BoundingBox bbox = boundingBoxForRegion(pm);
		
		int zCenter = zCenterFromMark(mark, bbox);
				
		return createStatisticsForBBox(
			pm,
			dim,
			bbox,
			zCenter
		);	
	}
	
	private static int zCenterFromMark( Mark markUncasted, BoundingBox bbox ) throws CreateException {
		if (!(markUncasted instanceof MarkAbstractPosition)) {
			throw new CreateException("Only marks that inherit from MarkAbstractPosition are supported");
		}
		
		MarkAbstractPosition mark = (MarkAbstractPosition) markUncasted;
		
		return (int) Math.round(mark.getPos().getZ()) - bbox.getCrnrMin().getZ();
	}
	
	protected abstract VoxelStatistics createStatisticsForBBox(PxlMark pm, ImageDim dim, BoundingBox bbox, int zCenter);
}
