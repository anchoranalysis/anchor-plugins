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


import georegression.fitting.ellipse.FitEllipseAlgebraic;
import georegression.struct.point.Point2D_F64;
import georegression.struct.shapes.EllipseQuadratic_F64;

import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point2d;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3f;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.orientation.Orientation2D;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleMatrix1D;
import ch.ethz.biol.cell.mpp.mark.Mark;
import ch.ethz.biol.cell.mpp.mark.MarkEllipse;

//
// Based upon the approach of Li and Griffiths in 'Least Squares Ellipsoid Specific Fitting' (2004)
//
//
// generalized eigenvalues in wolfram
// http://reference.wolfram.com/legacy/applications/anm/GeneralizedEigenvalueProblem/9.1.html
// ellipsoid_fit matlab function
// http://planetmath.org/EigenvalueProblem
// http://planetmath.org/node/4028
//http://en.wikipedia.org/wiki/Quadric
//
// To convert back into normal ellipsoid form
// http://www.mathworks.com/matlabcentral/fileexchange/45356-fitting-quadratic-curves-and-surfaces/content/ellipsoid_im2ex.m
//
public class LinearLeastSquaresEllipseFitterGeoRegression extends ConicFitterBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4978995087569551060L;

	// START BEAN
	@BeanField
	private double minRadius = 0.55;
	// END BEAN
	
	public LinearLeastSquaresEllipseFitterGeoRegression() {
		super();
		
	}
	

	
	
	@Override
	public void fit(List<Point3f> points, Mark mark, ImageDim dim)
			throws PointsFitterException, InsufficientPointsException {
		
		List<Point2D_F64> pntsConvert = new ArrayList<>();
		
		for( Point3f pnt : points ) {
			pntsConvert.add( new Point2D_F64(pnt.getX(), pnt.getY()));
		}
		
		// Help
		// http://georegression.org/javadoc/georegression/fitting/ellipse/FitEllipseAlgebraic.html
		
		FitEllipseAlgebraic fitter = new FitEllipseAlgebraic();
		fitter.process(pntsConvert);
		
		EllipseQuadratic_F64 fittedResult = fitter.getEllipse();
		
		// We assume if we can't fit to an ellipse, it's because there wasn't enough points
		if(!fittedResult.isEllipse()) {
			throw new InsufficientPointsException(
				String.format("Insufficient number of points for an ellipse-fit. There were %d points.", points.size())
			);
		}
		
		applyCoefficientsToMark(fittedResult, mark, dim);
	}
	
	private void applyCoefficientsToMark( EllipseQuadratic_F64 fittedResult, Mark mark, ImageDim dim ) throws PointsFitterException {
		
		//double div = fittedResult.f / -1; 
		
		// We create the coefficients by adding on a -1 at the end
		DoubleMatrix1D coefficients = DoubleFactory1D.dense.make(6);
		coefficients.set(0, fittedResult.a);
		coefficients.set(1, fittedResult.b*2);
		coefficients.set(2, fittedResult.c);
		coefficients.set(3, fittedResult.d*2);
		coefficients.set(4, fittedResult.e*2);
		coefficients.set(5, fittedResult.f);
		
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

	public double getMinRadius() {
		return minRadius;
	}

	public void setMinRadius(double minRadius) {
		this.minRadius = minRadius;
	}

	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return testMark instanceof MarkEllipse;
	}


}
