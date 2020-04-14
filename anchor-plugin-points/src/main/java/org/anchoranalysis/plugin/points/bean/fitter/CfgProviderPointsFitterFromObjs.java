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


import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.anchor.mpp.bean.cfg.CfgProvider;
import org.anchoranalysis.anchor.mpp.bean.mark.factory.MarkFactory;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point2i;
import org.anchoranalysis.core.geometry.Point3f;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.outline.FindOutline;
import org.anchoranalysis.image.points.PointsFromBinaryVoxelBox;

import ch.ethz.biol.cell.mpp.mark.pointsfitter.ConvexHullUtilities;

public class CfgProviderPointsFitterFromObjs extends CfgProvider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/// START BEAN PROPERTIES
	@BeanField
	private PointsFitterToMark pointsFitter;
	
	@BeanField
	private MarkFactory markFactory;
	
	/** If true, reduces the set of points by only taking points on the outline of the mask */
	@BeanField
	private boolean outline = true;
	
	/** If true, Reduces the set of points by applying a convex-hull operation */
	@BeanField
	private boolean convexHull = true;
	// END BEAN PROPERTIES
	
	@Override
	public Cfg create() throws CreateException {
		
		ImageDim dim = pointsFitter.createDim();

		ObjMaskCollection objsCollection = pointsFitter.createObjs();
		
		Cfg cfgOut = new Cfg();
		
		for( ObjMask om : objsCollection ) {
			Mark mark = createMarkFromObj(om,dim);
			if (mark!=null) {
				cfgOut.add( mark );
			}
		}

		return cfgOut;
	}
	
	private	Mark createMarkFromObj( ObjMask om, ImageDim dim ) throws CreateException {	
		
		List<Point2i> pts = pointsFromObj(om);
		
		if (pts.size()<pointsFitter.getMinNumPnts()) {
			return null;
		}
		
		return fitToMark(
			convertToFloat3D(pts),
			dim
		);
	}
	
	private List<Point2i> pointsFromObj( ObjMask om ) throws CreateException {
		List<Point2i> pts = new ArrayList<>();
		
		if (outline) {
			om = FindOutline.outline(om, 1, true, false);
		}
		
		PointsFromBinaryVoxelBox.addPointsFromVoxelBox( om.binaryVoxelBox(), om.getBoundingBox().getCrnrMin(), pts );
		
		if (convexHull) {
			pts = ConvexHullUtilities.convexHull2D(pts);
		}
		
		return pts;
	}
	
	private static List<Point3f> convertToFloat3D( List<Point2i> selectedPoints ) {
		List<Point3f> out = new ArrayList<>();
		for( Point2i pnt : selectedPoints ) {
			out.add( new Point3f( (float) pnt.getX(), (float) pnt.getY(), 0) );
		}
		return out;
	}
	
	private Mark fitToMark( List<Point3f> pntsToFit, ImageDim dim) throws CreateException {

		Mark markOut = markFactory.create();
		
		try {
			pointsFitter.fitPointsToMark( pntsToFit, markOut, dim );
		} catch (OperationFailedException e) {
			throw new CreateException(e);
		}
		
		return markOut;		
	}

	public MarkFactory getMarkFactory() {
		return markFactory;
	}

	public void setMarkFactory(MarkFactory markFactory) {
		this.markFactory = markFactory;
	}

	public boolean isOutline() {
		return outline;
	}

	public void setOutline(boolean outline) {
		this.outline = outline;
	}

	public boolean isConvexHull() {
		return convexHull;
	}

	public void setConvexHull(boolean convexHull) {
		this.convexHull = convexHull;
	}

	public PointsFitterToMark getPointsFitter() {
		return pointsFitter;
	}

	public void setPointsFitter(PointsFitterToMark pointsFitter) {
		this.pointsFitter = pointsFitter;
	}
}
