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

import org.anchoranalysis.anchor.mpp.bean.points.fitter.PointsFitterException;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipsoid;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.functional.Operation;
import org.anchoranalysis.core.geometry.Point3f;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.geometry.PointConverter;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.points.PointsFromObject;

import ch.ethz.biol.cell.mpp.mark.pointsfitter.LinearLeastSquaresEllipsoidFitter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
public class EllipsoidFactory {
	
	/**
	 * Creates a MarkEllipsoid using least-squares fitting to the points on the outline of an object-mask
	 * 
	 * @param object object-mask
	 * @param dim the dimensions of the scene the object is contaiend in
	 * @param suppressZCovariance  whether to suppress the covariance in the z-dimension when doing least squares fiting
	 * @param shellRad shellRad for the mark that is created
	 * @return
	 * @throws CreateException
	 */
	public static MarkEllipsoid createMarkEllipsoidLeastSquares( ObjectMask object, ImageDimensions dim, boolean suppressZCovariance, double shellRad ) throws CreateException {
		return createMarkEllipsoidLeastSquares(
			()->PointsFromObject.pointsFromMaskOutline(object),
			dim,
			suppressZCovariance,
			shellRad
		);
	}
		
	public static MarkEllipsoid createMarkEllipsoidLeastSquares(
		Operation<List<Point3i>,CreateException> opPoints,
		ImageDimensions dim,
		boolean suppressZCovariance,
		double shellRad
	) throws CreateException {
	
		LinearLeastSquaresEllipsoidFitter pointsFitter = new LinearLeastSquaresEllipsoidFitter();
		pointsFitter.setShellRad(shellRad);
		pointsFitter.setSuppressZCovariance(suppressZCovariance);
		
		List<Point3i> pts = opPoints.doOperation();
				
		// Now get all the points on the outline 
		MarkEllipsoid mark = new MarkEllipsoid();
		
		List<Point3f> pointsFloat = new ArrayList<>();
		pts.forEach( p->pointsFloat.add(
			PointConverter.floatFromInt(p)
		));
		
		try {
			pointsFitter.fit( pointsFloat, mark, dim );
		} catch (PointsFitterException e) {
			throw new CreateException(e);
		}
		return mark;
	}
}
