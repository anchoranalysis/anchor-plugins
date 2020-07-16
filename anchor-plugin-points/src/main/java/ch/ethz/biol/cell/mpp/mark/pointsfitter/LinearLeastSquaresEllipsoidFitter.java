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
import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipsoid;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.geometry.Point3f;
import org.anchoranalysis.image.extent.ImageDimensions;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.EigenvalueDecomposition;
import cern.jet.math.Functions;
import lombok.Getter;
import lombok.Setter;

/**
 * Fits an ellipsoid to points using a linear least squares approach
 * <p>
 * Specifically the approach of Li and Griffiths in 'Least Squares Ellipsoid Specific Fitting' (2004) is employed.
 * <p>
 * Some references
 * <ul>
 * <li><a href="http://reference.wolfram.com/legacy/applications/anm/GeneralizedEigenvalueProblem/9.1.html">generalized eigenvalues in wolfram</a>
 * <li><a href="https://ch.mathworks.com/matlabcentral/fileexchange/24693-ellipsoid-fit">ellipsoid_fit matlab function</a>
 * <li><a href="https://ch.mathworks.com/matlabcentral/fileexchange/45356-fitting-quadratic-curves-and-surfaces">To convert back into normal ellipsoid form</a>
 * </ul>
 * 
 * @author Owen Feehan
 *
 */
public class LinearLeastSquaresEllipsoidFitter extends ConicFitterBase {

	// START BEAN PROPERTIES
	@BeanField @Getter @Setter
	private double minRadius = 0.55;
	
	@BeanField @Getter @Setter
	private boolean suppressZCovariance = false;
	// END BEAN PROPERTIES
	
	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return testMark instanceof MarkEllipsoid;
	}
	
	@Override
	public void fit(List<Point3f> points, Mark mark, ImageDimensions dimensions) throws PointsFitterException {

		DoubleMatrix2D matCInverse = InverseHelper.inverseFor(
			MatrixCreator.createConstraintMatrix().viewPart(0, 0, 6, 6)
		);
		
		DoubleMatrix2D matD = MatrixCreator.createDesignMatrixWithOnes(points, (float) getSubtractRadii() );
		
		DoubleMatrix2D matS = matD.viewDice().zMult(matD, null);
		
		createFitResultFromMatS(matS, matCInverse).applyFitResultToMark(
			(MarkEllipsoid) mark,
			dimensions,
			getShellRad()
		);
	}
	
	private FitResult createFitResultFromMatS(DoubleMatrix2D matS, DoubleMatrix2D matCInverse) throws PointsFitterException {
		// We index regions in this matrix using the same terminology as the paper s_11, s_12 etc.
		DoubleMatrix2D matS11 = matS.viewPart(0, 0, 6, 6);
		DoubleMatrix2D matS12 = matS.viewPart(0, 6, 6, 4);
		DoubleMatrix2D matS21 = matS.viewPart(6, 0, 4, 6);
		DoubleMatrix2D matS22 = matS.viewPart(6, 6, 4, 4);
		
		DoubleMatrix2D matS22Inv = InverseHelper.inverseFor(matS22);
		matS22Inv.assign( Functions.mult(-1) );
		
		// Solve generalized eigenvalue/eigenvector problem
		DoubleMatrix2D mult1 = matS22Inv.zMult(matS21, null);
		
		DoubleMatrix2D mult3 = createMult3(
			mult1,
			matCInverse,
			matS11,
			matS12
		);
		
		return createFitResultFromDecomposition(mult1,mult3);
	}
	
	private DoubleMatrix2D createMult3(DoubleMatrix2D mult1, DoubleMatrix2D matCInverse, DoubleMatrix2D matS11, DoubleMatrix2D matS12) {
		DoubleMatrix2D mult2 = matS12.zMult( mult1, null);
		mult2.assign( matS11, Functions.plus );
		return matCInverse.zMult(mult2, null);		
	}
	
	private FitResult createFitResultFromDecomposition(DoubleMatrix2D mult1, DoubleMatrix2D mult3) throws PointsFitterException {
		EigenvalueDecomposition e = new EigenvalueDecomposition(mult3); 
		
		int index = selectEigenVector( e.getRealEigenvalues() );
		if (index==-1) {
			throw new PointsFitterException("Cannot find suitable eigen-value");
		}
		
		DoubleMatrix1D v1 = e.getV().viewColumn(index);
		DoubleMatrix1D v2 = mult1.zMult(v1, null);
		return createFitResult(v1,v2);
	}
	
	private FitResult createFitResult(DoubleMatrix1D v1, DoubleMatrix1D v2) throws PointsFitterException {
		
		DoubleMatrix2D matA = MatrixCreator.createMatrixA(v1, v2);
		
		FitResult fitResult = EllipsoidFitHelper.createFitResultFromMatrixAandCenter(
			matA,
			MatrixCreator.createMatrixCenter(matA, v2),
			suppressZCovariance
		);
		fitResult.applyRadiiSubtractScale( getSubtractRadii(), getScaleRadii() );
		fitResult.imposeMinimumRadius( minRadius );
		return fitResult;
	}
			
	/** 
	 * Selects which eigen vector to use, using the method from the Li and Griffiths paper. 
	 * <p>
	 * In most cases there should be only a single positive eigen value, which we take
	 * But in other situations we take the vector with the largest eigenvalue
	 * 
	 * @param  eigenvalues matrix containing eigen-values
	 * @return index of which eigen-vector to use
	 **/
	private int selectEigenVector( DoubleMatrix1D eigenvalues ) {
		
		int maxIndex = -1;
		double maxValue = 0;
		for( int i=0; i<eigenvalues.size(); i++) {
			double val = eigenvalues.get(i);
			if (i==0 || val > maxValue) {
				maxValue = val;
				maxIndex = i;
			}
		}
		return maxIndex;
	}
}
