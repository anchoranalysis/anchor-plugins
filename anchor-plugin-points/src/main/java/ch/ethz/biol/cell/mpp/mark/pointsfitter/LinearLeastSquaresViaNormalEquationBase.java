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
import org.anchoranalysis.core.geometry.Point3f;
import org.anchoranalysis.image.extent.ImageDim;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

public abstract class LinearLeastSquaresViaNormalEquationBase extends ConicFitterBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// This solves the normal-equation for a Linear Least Squares system where all our dependent variables
	//   are treated as 1 (solution to the ellipsoid matrix equation).  It returns a matrix of our optimal coefficients
	protected static DoubleMatrix2D solveNormalEquation( DoubleMatrix2D matrixD ) throws PointsFitterException {
		DoubleMatrix2D num = matrixD.viewDice().zMult( matrixD, null);
		DoubleMatrix2D ones = DoubleFactory2D.dense.make( matrixD.rows(), 1, 1 );
		DoubleMatrix2D dem = matrixD.viewDice().zMult( ones, null );
		return matrixLeftDivide(num,dem);
	}
	
	@Override
	public void fit(List<Point3f> points, Mark mark, ImageDim dim)
			throws PointsFitterException {
		
		if (points.size() < minNumPoints()) {
			throw new PointsFitterException( String.format("Must have at least %d points to fit", minNumPoints()) );
		}
		

		DoubleMatrix2D matrixD = createDesignMatrix(points);

		DoubleMatrix2D coefficients = solveNormalEquation(matrixD);
		applyCoefficientsToMark( coefficients, mark, dim);
	}
	
	protected abstract int minNumPoints();
	
	protected abstract void applyCoefficientsToMark(  DoubleMatrix2D matrixV, Mark mark, ImageDim dim ) throws PointsFitterException;
	
	protected abstract DoubleMatrix2D createDesignMatrix( List<Point3f> points );
}
