package org.anchoranalysis.plugin.points.calculate.ellipsoid;

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

import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipsoid;
import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.core.cache.Operation;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point3f;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.geometry.PointConverter;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.points.PointsFromObjMask;

import ch.ethz.biol.cell.mpp.mark.pointsfitter.LinearLeastSquaresEllipsoidFitter;
import ch.ethz.biol.cell.mpp.mark.pointsfitter.PointsFitterException;

public class EllipsoidFactory {
	
	/**
	 * Creates a MarkEllipsoid using least-squares fitting to the points on the outline of an object-mask
	 * 
	 * @param om object-mask
	 * @param dim the dimensions of the scene the object is contaiend in
	 * @param suppressZCovariance  whether to suppress the covariance in the z-dimension when doing least squares fiting
	 * @param shellRad shellRad for the mark that is created
	 * @return
	 * @throws CreateException
	 */
	public static MarkEllipsoid createMarkEllipsoidLeastSquares( ObjMask om, ImageDim dim, boolean suppressZCovariance, double shellRad ) throws CreateException {
		Operation<List<Point3i>> opPnts = ()->pntsFromMaskOutlineWrapped(om);
		return createMarkEllipsoidLeastSquares( opPnts, dim, suppressZCovariance, shellRad );
	}
	
	
	public static MarkEllipsoid createMarkEllipsoidLeastSquares( Operation<List<Point3i>> opPnts, ImageDim dim, boolean suppressZCovariance, double shellRad ) throws CreateException {
	
		LinearLeastSquaresEllipsoidFitter pointsFitter = new LinearLeastSquaresEllipsoidFitter();
		pointsFitter.setShellRad(shellRad);
		pointsFitter.setSuppressZCovariance(suppressZCovariance);
		
		List<Point3i> pts;
		try {
			pts = opPnts.doOperation();
		} catch (ExecuteException e1) {
			throw new CreateException(e1);
		}
				
		// Now get all the points on the outline 
		MarkEllipsoid mark = new MarkEllipsoid();
		
		List<Point3f> ptsF = new ArrayList<>();
		for( Point3i p : pts ) {
			ptsF.add(
				PointConverter.floatFromInt(p)
			);
		}
		
		try {
			pointsFitter.fit( ptsF, mark, dim );
		} catch (PointsFitterException e) {
			throw new CreateException(e);
		}

		return mark;
	}
	
	private static List<Point3i> pntsFromMaskOutlineWrapped( ObjMask om ) throws ExecuteException {
		try {
			return PointsFromObjMask.pntsFromMaskOutline(om);
		} catch (CreateException e) {
			throw new ExecuteException(e);
		}
	}
	
}
