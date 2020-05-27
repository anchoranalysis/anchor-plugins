package org.anchoranalysis.plugin.mpp.bean.proposer.points;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.anchoranalysis.anchor.mpp.bean.bound.BoundUnitless;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.points.BoundingBoxFromPoints;
import org.anchoranalysis.image.points.PointsFromBinaryChnl;

class GeneratePointsHelper {

	private GeneratePointsHelper() {}
	
	public static List<Point3i> generatePoints(
		Point3d pntRoot,
		List<List<Point3i>> pntsXY,
		int maxZDist,
		int skipZDist,
		BinaryChnl chnl,
		Optional<BinaryChnl> chnlFilled,
		ImageDim dim,
		int forceMinZSize
	) throws OperationFailedException {
		
		// We take the first point in each list, as where it intersects with the edge
		PointListForConvex pl = pointListForConvex(pntsXY);
		
		
		List<Point3i> lastPntsAll = new ArrayList<Point3i>();
		//lastPntsAll.clear();
		
		for( List<Point3i> contourPnts : pntsXY ) {
			try {
				lastPntsAll.addAll(
					extendedPoints( contourPnts, pntRoot, pl, maxZDist, skipZDist, chnl, chnlFilled, dim )
				);
			} catch (CreateException e) {
				throw new OperationFailedException(e);
			}
		}
		
		
		BoundUnitless bound = zSize(lastPntsAll);
		if (bound.size()<forceMinZSize) {
			lastPntsAll = ForceMinimumSizeHelper.forceMinimumZSize(lastPntsAll, bound, forceMinZSize);	
		}
		return lastPntsAll;
	}
	
	
	private static List<Point3i> extendedPoints( List<Point3i> pntsAlongContour, Point3d pntRoot, PointListForConvex pl, int maxZDist, int skipZDist, BinaryChnl chnl, Optional<BinaryChnl> chnlFilled, ImageDim sceneDim ) throws OperationFailedException, CreateException {
		
		BoundingBox bbox = BoundingBoxFromPoints.forList(pntsAlongContour);

		int zLow = Math.max(0, bbox.getCrnrMin().getZ()-maxZDist );
		int zHigh = Math.min(sceneDim.getZ(), bbox.getCrnrMin().getZ()+maxZDist );
		
		Extent e = bbox.extent().duplicateChangeZ(zHigh-zLow);
		bbox.getCrnrMin().setZ( zLow );
		bbox.setExtent(e);

		if (!chnlFilled.isPresent()) {
			return PointsFromBinaryChnl.pointsFromChnlInsideBox(chnl, bbox, (int) Math.floor(pntRoot.getZ()), skipZDist);
		}
		
		return PointsFromInsideHelper.convexOnly(chnl, chnlFilled.get(), bbox, pntRoot, pl, skipZDist);
	}
	
	private static PointListForConvex pointListForConvex( List<List<Point3i>> pnts ) {
		PointListForConvex pl = new PointListForConvex();
		for( List<Point3i> list : pnts ) {
			if (list.size()>0) {
				pl.add( list.get(0) );
			}
		}
		return pl;
	}

	private static BoundUnitless zSize( List<Point3i> pnts ) {
		
		boolean first = true;
		int min = 0;
		int max = 0;
		for( Point3i p : pnts) {
			if (first) {
				min = p.getZ();
				max = p.getZ();
				first = false;
			} else {
				if (p.getZ() <min) {
					min=p.getZ();
				}
				if (p.getZ()>max) {
					max=p.getZ();
				}				
			}
		}
		return new BoundUnitless(min,max);
	}
}
