package ch.ethz.biol.cell.mpp.mark.pointsfitter;

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

import org.anchoranalysis.anchor.mpp.bean.points.fitter.PointsFitterException;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipse;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point2d;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3f;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.orientation.Orientation2D;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;

// ellipsoid_fit matlab function
//
// see http://en.wikipedia.org/wiki/Matrix_representation_of_conic_sections
public class LinearLeastSquaresEllipseFitter extends LinearLeastSquaresViaNormalEquationBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4978995087569551060L;
	
	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return testMark instanceof MarkEllipse;
	}

	@Override
	protected void applyCoefficientsToMark(  DoubleMatrix2D matrixV, Mark mark, ImageDim dim ) throws PointsFitterException {
		
		// We create the coefficients by adding on a -1 at the end
		DoubleMatrix1D coefficients = DoubleFactory1D.dense.make(6);
		for( int i=0; i<5; i++) {
			coefficients.set(i, matrixV.get(i, 0));
		}
		coefficients.set(5, -1);

		
		// We convert the coefficients to more useful geometric properties		
		EllipseStandardFormConverter c = new EllipseStandardFormConverter(coefficients);
		try {
			c.convert();
		} catch (OperationFailedException e) {
			throw new PointsFitterException(e);
		}			
		
		assert( !Double.isNaN(c.getMajorAxisAngle()) );
		
		double radiusX = c.getSemiMajorAxis() - getSubtractRadii();
		double radiusY = c.getSemiMinorAxis() - getSubtractRadii();
		if (radiusX<=0 || radiusY<=0) {
			throw new PointsFitterException("fitter returned 0 width or height");
		}
		
		
		// Put values onto the Mark Ellipse
		MarkEllipse markE = (MarkEllipse) mark;
		markE.setShellRad(getShellRad());
		markE.setMarksExplicit(
			new Point3d( c.getCenterPointX(), c.getCenterPointY(), 0),
			new Orientation2D( c.getMajorAxisAngle() ), // the reason for -1, assuming clockwise/anti-clockwise incompatibility
			new Point2d( radiusX, radiusY )
		);
	}

	@Override
	protected DoubleMatrix2D createDesignMatrix( List<Point3f> points ) {
		
		// The columns are as follows: x^2 xy y^2 x y 1
		DoubleMatrix2D matrix = DoubleFactory2D.dense.make( points.size(), 5 ); 
		
		for( int i=0; i<points.size(); i++) {
			Point3f pnt = points.get(i);
			
			float x = pnt.getX() + getInputPointShift();
			float y = pnt.getY() + getInputPointShift();
			
			matrix.set(i, 0, Math.pow( x, 2) );
			matrix.set(i, 1, x * y );
			matrix.set(i, 2, Math.pow( y, 2) );
			matrix.set(i, 3, x );
			matrix.set(i, 4, y );
		}
		
		return matrix;
	}

	@Override
	protected int minNumPoints() {
		return 6;
	}

}
