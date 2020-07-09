package org.anchoranalysis.plugin.mpp.bean.proposer.points;

/*-
 * #%L
 * anchor-plugin-mpp
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.points.BoundingBoxFromPoints;
import org.anchoranalysis.image.points.PointsFromBinaryChnl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
class GeneratePointsHelper {
	
	public static List<Point3i> generatePoints(
		Point3d pntRoot,
		List<List<Point3i>> pntsXY,
		int maxZDist,
		int skipZDist,
		BinaryChnl chnl,
		Optional<BinaryChnl> chnlFilled,
		ImageDimensions dim
	) throws OperationFailedException {
		// We take the first point in each list, as where it intersects with the edge
		PointListForConvex pointList = pointListForConvex(pntsXY);
		
		List<Point3i> lastPntsAll = new ArrayList<>();
		
		for( List<Point3i> contourPnts : pntsXY ) {
			lastPntsAll.addAll(
				extendedPoints(
					contourPnts,
					pntRoot,
					pointList,
					maxZDist,
					skipZDist,
					chnl,
					chnlFilled,
					dim
				)
			);
		}
		return lastPntsAll;
	}
	
	
	private static List<Point3i> extendedPoints(
		List<Point3i> pntsAlongContour,
		Point3d pntRoot,
		PointListForConvex pl,
		int maxZDist,
		int skipZDist,
		BinaryChnl chnl,
		Optional<BinaryChnl> chnlFilled,
		ImageDimensions sceneDim
	) throws OperationFailedException {
		
		BoundingBox bbox = BoundingBoxFromPoints.forList(pntsAlongContour);

		int zLow = Math.max(0, bbox.cornerMin().getZ()-maxZDist );
		int zHigh = Math.min(sceneDim.getZ(), bbox.cornerMin().getZ()+maxZDist );

		if (chnlFilled.isPresent()) {
			return PointsFromInsideHelper.convexOnly(
				chnl,
				chnlFilled.get(),
				bbox,
				pntRoot,
				pl,
				skipZDist
			);
		} else {
			return PointsFromBinaryChnl.pointsFromChnlInsideBox(
				chnl,
				bbox.duplicateChangeZ(zLow, zHigh-zLow),
				(int) Math.floor(pntRoot.getZ()),
				skipZDist
			);
		}
	}
	
	private static PointListForConvex pointListForConvex( List<List<Point3i>> pnts ) {
		PointListForConvex pl = new PointListForConvex();
		for( List<Point3i> list : pnts ) {
			if (!list.isEmpty()) {
				pl.add( list.get(0) );
			}
		}
		return pl;
	}
}
