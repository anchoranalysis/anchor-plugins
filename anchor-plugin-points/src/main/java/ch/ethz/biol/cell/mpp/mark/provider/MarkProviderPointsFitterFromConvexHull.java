package ch.ethz.biol.cell.mpp.mark.provider;

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

import org.anchoranalysis.anchor.mpp.bean.points.fitter.InsufficientPointsException;
import org.anchoranalysis.anchor.mpp.bean.points.fitter.PointsFitter;
import org.anchoranalysis.anchor.mpp.bean.points.fitter.PointsFitterException;
import org.anchoranalysis.anchor.mpp.bean.provider.MarkProvider;
import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMap;
import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMembership;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point2i;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3f;
import org.anchoranalysis.core.geometry.PointConverter;
import org.anchoranalysis.image.bean.provider.ImageDimProvider;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.objmask.ObjMaskCollection;

import ch.ethz.biol.cell.mpp.mark.pointsfitter.ConvexHullUtilities;

// Applies a points fitter on the convex hull of some objects
// Only works in 2D for now
public class MarkProviderPointsFitterFromConvexHull extends MarkProvider {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private MarkProvider markProvider;
	
	@BeanField
	private ObjMaskProvider objs;
	
	@BeanField
	private PointsFitter pointsFitter;
	
	@BeanField
	private ImageDimProvider dimProvider;
	
	@BeanField
	private int minNumPnts;
	
	@BeanField
	private double minRatioPntsInsideRegion;
	
	@BeanField
	private int regionID = 0;
	
	@BeanField
	private RegionMap regionMap;
	// END BEAN PROPERTIES
	
	@Override
	public Mark create() throws CreateException {

		Mark mark = markProvider.create();
	
		ObjMaskCollection objsCollection = objs.create();
		
		List<Point2i> selectedPoints = ConvexHullUtilities.convexHullFromAllOutlines(objsCollection, minNumPnts);
				
		List<Point3f> pntsForFitter = PointConverter.convert2i_3f(selectedPoints);
		
		try {
			pointsFitter.fit( pntsForFitter, mark, dimProvider.create() );
		} catch (PointsFitterException | InsufficientPointsException e) {
			throw new CreateException(e);
		}
		
		
		if (minRatioPntsInsideRegion>0) {
			// We perform a check that a minimum % of pnts are inside a particular region
			double ratioInside = ratioPointsInsideRegion( mark, pntsForFitter, regionMap, regionID );
			
			if (ratioInside<minRatioPntsInsideRegion) {
				return null;
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
	
	public MarkProvider getMarkProvider() {
		return markProvider;
	}


	public void setMarkProvider(MarkProvider markProvider) {
		this.markProvider = markProvider;
	}


	public ObjMaskProvider getObjs() {
		return objs;
	}


	public void setObjs(ObjMaskProvider objs) {
		this.objs = objs;
	}


	public PointsFitter getPointsFitter() {
		return pointsFitter;
	}

	public void setPointsFitter(PointsFitter pointsFitter) {
		this.pointsFitter = pointsFitter;
	}

	public int getMinNumPnts() {
		return minNumPnts;
	}


	public void setMinNumPnts(int minNumPnts) {
		this.minNumPnts = minNumPnts;
	}


	public double getMinRatioPntsInsideRegion() {
		return minRatioPntsInsideRegion;
	}


	public void setMinRatioPntsInsideRegion(double minRatioPntsInsideRegion) {
		this.minRatioPntsInsideRegion = minRatioPntsInsideRegion;
	}


	public int getRegionID() {
		return regionID;
	}


	public void setRegionID(int regionID) {
		this.regionID = regionID;
	}


	public RegionMap getRegionMap() {
		return regionMap;
	}


	public void setRegionMap(RegionMap regionMap) {
		this.regionMap = regionMap;
	}


	public ImageDimProvider getDimProvider() {
		return dimProvider;
	}


	public void setDimProvider(ImageDimProvider dimProvider) {
		this.dimProvider = dimProvider;
	}






}
