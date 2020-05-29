package org.anchoranalysis.plugin.points.bean.fitter;

/*
 * #%L
 * anchor-plugin-points
 * %%
 * Copyright (C) 2016 ETH Zurich, University of Zurich, Owen Feehan
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


import java.util.List;
import java.util.Optional;

import org.anchoranalysis.anchor.mpp.bean.provider.MarkProvider;
import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMap;
import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMembership;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point2i;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3f;
import org.anchoranalysis.core.geometry.PointConverter;

import ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider.ConvexHullUtilities;

// Applies a points fitter on the convex hull of some objects
// Only works in 2D for now
public class MarkProviderPointsFitterFromConvexHull extends MarkProvider {
	
	// START BEAN PROPERTIES
	@BeanField
	private PointsFitterToMark pointsFitter;
	
	@BeanField
	private MarkProvider markProvider;
	
	@BeanField
	private double minRatioPntsInsideRegion;
	
	@BeanField
	private RegionMap regionMap;
		
	@BeanField
	private int regionID = 0;
	// END BEAN PROPERTIES
	
	@Override
	public Optional<Mark> create() throws CreateException {

		Optional<List<Point3f>> pntsForFitter = pointsForFitter();
		Optional<Mark> mark = markProvider.create();
		
		if (!mark.isPresent() || !pntsForFitter.isPresent()) {
			return Optional.empty();
		}
		
		try {
			pointsFitter.fitPointsToMark(
				pntsForFitter.get(),
				mark.get(),
				pointsFitter.createDim()
			);
		} catch (OperationFailedException | CreateException e) {
			throw new CreateException(e);
		}
		
		if (minRatioPntsInsideRegion>0) {
			// We perform a check that a minimum % of pnts are inside a particular region
			double ratioInside = ratioPointsInsideRegion(
				mark.get(),
				pntsForFitter.get(),
				regionMap,
				regionID
			);
			
			if (ratioInside<minRatioPntsInsideRegion) {
				return Optional.empty();
			}
		}
		return mark;
	}
	
	public static double ratioPointsInsideRegion( Mark m, List<Point3f> pnts, RegionMap regionMap, int regionID ) {
	
		RegionMembership rm = regionMap.membershipForIndex(regionID);
		byte flags = rm.flags();
		
		int cnt = 0;
		for( Point3f pnt : pnts ) {
			Point3d pntD = new Point3d( pnt );
			byte membership = m.evalPntInside(pntD);
			
			if (rm.isMemberFlag(membership, flags)) {
				cnt++;
			}
		}
		
		return ((double) cnt) / pnts.size();
	}
	
	private Optional<List<Point3f>> pointsForFitter() throws CreateException {
		
		Optional<List<Point2i>> selectedPoints = ConvexHullUtilities.convexHullFromAllOutlines(
			pointsFitter.createObjs(),
			pointsFitter.getMinNumPnts()
		);
		return selectedPoints.map(PointConverter::convert2i_3f);
	}
	
	public MarkProvider getMarkProvider() {
		return markProvider;
	}

	public void setMarkProvider(MarkProvider markProvider) {
		this.markProvider = markProvider;
	}

	public double getMinRatioPntsInsideRegion() {
		return minRatioPntsInsideRegion;
	}

	public void setMinRatioPntsInsideRegion(double minRatioPntsInsideRegion) {
		this.minRatioPntsInsideRegion = minRatioPntsInsideRegion;
	}

	public RegionMap getRegionMap() {
		return regionMap;
	}

	public void setRegionMap(RegionMap regionMap) {
		this.regionMap = regionMap;
	}

	public PointsFitterToMark getPointsFitter() {
		return pointsFitter;
	}

	public void setPointsFitter(PointsFitterToMark pointsFitter) {
		this.pointsFitter = pointsFitter;
	}
		
	public int getRegionID() {
		return regionID;
	}

	public void setRegionID(int regionID) {
		this.regionID = regionID;
	}
}
