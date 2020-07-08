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
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3f;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.orientation.Orientation;
import org.anchoranalysis.image.orientation.OrientationRotationMatrix;
import org.anchoranalysis.math.rotation.RotationMatrix;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.colt.matrix.linalg.EigenvalueDecomposition;
import cern.colt.matrix.linalg.SingularValueDecomposition;
import cern.jet.math.Functions;

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
public class LinearLeastSquaresEllipsoidFitter extends ConicFitterBase {

	// START BEAN
	@BeanField
	private double minRadius = 0.55;
	
	@BeanField
	private boolean suppressZCovariance = false;
	// END BEAN
	
	private Algebra algebra = new Algebra();
	
	private double machineEpsilon;
	
	public LinearLeastSquaresEllipsoidFitter() {
		super();
		machineEpsilon = calculateMachineEpsilonFloat();
	}
	

	private static float calculateMachineEpsilonFloat() {
        float machEps = 1.0f;
 
        do
           machEps /= 2.0f;
        while ((float) (1.0 + (machEps / 2.0)) != 1.0);
 
        return machEps;
    }
	
	public DoubleMatrix2D createConstraintMatrix() {
	
		DoubleMatrix2D mat = DoubleFactory2D.dense.make( 10, 10 );
		for( int i=0; i<6; i++) {
			mat.set(i, i, -1);
		}
		mat.set(0, 1, 1);
		mat.set(0, 2, 1);
		mat.set(1, 0, 1);
		mat.set(1, 2, 1);
		mat.set(2, 0, 1);
		mat.set(2, 1, 1);
		return mat;
	}

	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return testMark instanceof MarkEllipsoid;
	}
	
	static FitResult createFitResultFromMatrixAandCenter( DoubleMatrix2D matrixA, DoubleMatrix2D matrixCenter, boolean suppressZCovariance ) throws PointsFitterException {

		DoubleMatrix2D matrixR = createMatrixR( matrixA, matrixCenter  );
		
		FitResult fitResult = new FitResult();
		{
			double divVal = matrixR.get(3, 3) * -1;
			DoubleMatrix2D e = matrixR.viewPart(0, 0, 3, 3);
			
			e.assign( cern.jet.math.Functions.div(divVal) );
			
			if (suppressZCovariance) {
				e.set(2, 0, 0);
				e.set(2, 1, 0);
				e.set(0, 2, 0);
				e.set(1, 2, 0);
			}
			
			EigenvalueDecomposition evd;
			try {
				evd = new EigenvalueDecomposition( e );
			} catch (ArrayIndexOutOfBoundsException e1) {
				throw new PointsFitterException( String.format("Cannot init EigenValueDecomposition with arg='%s'",e.toString()));
			}
			
			// Math.abs
			fitResult.setRadiusX( Math.sqrt( 1/ Math.abs(evd.getD().get(0, 0))));
			fitResult.setRadiusY( Math.sqrt( 1/ Math.abs(evd.getD().get(1, 1))));
			fitResult.setRadiusZ( Math.sqrt( 1/ Math.abs(evd.getD().get(2, 2))));
			
			fitResult.setCentrePnt( new Point3d(
				matrixCenter.get(0, 0),
				matrixCenter.get(1, 0),
				matrixCenter.get(2, 0)
			) );
			
			fitResult.setRotMatrix( evd.getV() );
			
			assert( !Double.isNaN(fitResult.getRadiusX()) );
			assert( !Double.isNaN(fitResult.getRadiusY()) );
			assert( !Double.isNaN(fitResult.getRadiusZ()) );
			assert( fitResult.getRadiusX() > 0 );
			assert( fitResult.getRadiusY() > 0 );
			assert( fitResult.getRadiusZ() > 0 );
			
			
		}
		return fitResult;
	}

	private static DoubleMatrix2D createMatrixR( DoubleMatrix2D matrixA, DoubleMatrix2D matrixCenter ) {
		
		DoubleMatrix2D matrixT = DoubleFactory2D.dense.identity(4);
		
		matrixT.set(3, 0, matrixCenter.get(0, 0) );
		matrixT.set(3, 1, matrixCenter.get(1, 0) );
		matrixT.set(3, 2, matrixCenter.get(2, 0) );
		
		return matrixT.zMult(matrixA, null).zMult(matrixT.viewDice(), null);
	}
	
	// Calculates the pseudoInverse of a diagonal matrix
	// NOTE Changes the existing matrix inplace
	private DoubleMatrix2D pesudoInverseDiag( DoubleMatrix2D mat ) {
		
		double maxVal = Double.MIN_VALUE;
		for( int i=0; i<mat.columns(); i++) {
			double val = mat.get(i, i);	
			if (val>maxVal) {
				maxVal = val;
			}
		}
		
		double tol = machineEpsilon * Math.max( mat.columns(), mat.rows() ) * maxVal;
			
		for( int i=0; i<mat.columns(); i++) {
			double val = mat.get(i, i);
			if (val>tol) {
				mat.set(i,i, 1/val);
			} else {
				mat.set(i,i, 0);
			}
		}
		return mat;
	}
	
	// Calculates Moore-Penrose pseudoinverse
	// Uses instructions (SVD approach) in:
	//  http://en.wikipedia.org/wiki/Moore%E2%80%93Penrose_pseudoinverse
	private DoubleMatrix2D pesudoInverse( DoubleMatrix2D mat ) {
		SingularValueDecomposition svd = new SingularValueDecomposition(mat);
		return svd.getV().zMult(pesudoInverseDiag(svd.getS()), null).zMult( svd.getU().viewDice(), null );
	}
	
	@Override
	public void fit(List<Point3f> points, Mark mark, ImageDimensions dim)
			throws PointsFitterException {
		
		DoubleMatrix2D matD = createDesignMatrixWithOnes(points, (float) getSubtractRadii() );
		
		DoubleMatrix2D matS = matD.viewDice().zMult( matD, null);
		
		DoubleMatrix2D matC = createConstraintMatrix();
		
		DoubleMatrix2D matCInverse = algebra.inverse( matC.viewPart(0, 0, 6, 6) );
		
		DoubleMatrix2D matS11 = matS.viewPart(0, 0, 6, 6);
		DoubleMatrix2D matS12 = matS.viewPart(0, 6, 6, 4);
		DoubleMatrix2D matS21 = matS.viewPart(6, 0, 4, 6);
		DoubleMatrix2D matS22 = matS.viewPart(6, 6, 4, 4);
		
		DoubleMatrix2D matS22Inv;
		if (new Algebra().det(matS22)>1e-9) {
			matS22Inv = algebra.inverse(matS22);
		} else {
			// Otherwise we calculate a pseudo-inverse and of matS_22 and assign it to matS_22_inv
			matS22Inv = pesudoInverse(matS22);
		}
		
		
		matS22Inv.assign( Functions.mult(-1) );
		
		// Solve generalized eigenvalue/eigenvector problem
		DoubleMatrix2D mult1 = matS22Inv.zMult(matS21, null);
		DoubleMatrix2D mult2 = matS12.zMult( mult1, null);
				mult2.assign( matS11, Functions.plus );
		
		DoubleMatrix2D mult3 = matCInverse.zMult(mult2, null);
		
		EigenvalueDecomposition e = new EigenvalueDecomposition(mult3); 
		
		int index = getEigenVectorIndex( e.getRealEigenvalues() );
		if (index==-1) {
			throw new PointsFitterException("Cannot find suitable eigen-value");
		}
		
		DoubleMatrix1D v1 = e.getV().viewColumn(index);
		
		DoubleMatrix1D v2 = mult1.zMult(v1, null);
		
		DoubleMatrix2D matA = createMatrixA(v1, v2);
		
		DoubleMatrix2D matCenter = createMatrixCenter(matA, v2);
		
		FitResult fitResult = createFitResultFromMatrixAandCenter(matA, matCenter, suppressZCovariance);

		fitResult.applyRadiiSubtractScale( getSubtractRadii(), getScaleRadii() );
		fitResult.imposeMinimumRadius( minRadius );
		
		applyFitResultToMark( fitResult, mark, dim, getShellRad() );
	}
	
	
	private static DoubleMatrix2D createMatrixCenter( DoubleMatrix2D matrixA, DoubleMatrix1D v2 ) throws PointsFitterException {
		
		DoubleMatrix2D first = matrixA.viewPart(0,0,3,3).copy().assign( cern.jet.math.Functions.mult(-1) );
		
		DoubleMatrix2D second = DoubleFactory2D.dense.make( 3, 1 );
		second.viewColumn(0).assign( v2.viewPart(0, 3) );
		
		return matrixLeftDivide(first, second);
	}
	
	
	protected static DoubleMatrix2D createDesignMatrixWithOnes( List<Point3f> points, float inputPointShift ) {
		
		// The columns are as follows: x^2 xy y^2 x y 1
		DoubleMatrix2D matrix = DoubleFactory2D.dense.make( points.size(), 10 ); 
		
		for( int i=0; i<points.size(); i++) {
			Point3f pnt = points.get(i);
			
			float x = pnt.getX() + inputPointShift;
			float y = pnt.getY() + inputPointShift;
			float z = pnt.getZ() + inputPointShift;
			
			matrix.set(i, 0, Math.pow( x, 2) );	// xx
			matrix.set(i, 1, Math.pow( y, 2) );	// yy
			matrix.set(i, 2, Math.pow( z, 2) );	// zz
			
			matrix.set(i, 3, 2 * y * z );	// 2yz
			matrix.set(i, 4, 2 * x * z );	// 2xz
			matrix.set(i, 5, 2 * x * y );	// 2xy
			
			matrix.set(i, 6, 2 * x );	// 2x
			matrix.set(i, 7, 2 * y );	// 2y
			matrix.set(i, 8, 2 * z );	// 2z
			
			matrix.set(i, 9, 1 );	// 1
		}
		
		return matrix;
	}
	
	
	private static DoubleMatrix2D createMatrixA( DoubleMatrix1D v1, DoubleMatrix1D v2 ) {
		
		DoubleMatrix2D m = DoubleFactory2D.dense.make(4, 4);
		
		// Diagonals
		m.set(0, 0, v1.get(0) );	// xx
		m.set(1, 1, v1.get(1) );	// yy
		m.set(2, 2, v1.get(2) );	// zz
		m.set(3, 3, v2.get(3) );
		
		m.set(1, 0, v1.get(5) );	// 2xy
		m.set(0, 1, v1.get(5) );	// 2xy
		
		m.set(2, 0, v1.get(4) );	// 2xz
		m.set(0, 2, v1.get(4) );	// 2xz 
		
		m.set(3, 0, v2.get(0) );	// 2x
		m.set(0, 3, v2.get(0) );	// 2x
		
		m.set(1, 2, v1.get(3) );	// 2yz
		m.set(2, 1, v1.get(3) );	// 2yz
		
		m.set(1, 3, v2.get(1) );	// 2y
		m.set(3, 1, v2.get(1) );	// 2y
		
		m.set(2, 3, v2.get(2) );	// 2z
		m.set(3, 2, v2.get(2) );	// 2z
		
		return m;
	}
	
	// From the paper
	// In most cases there should be only a single positive eigen value, which we take
	// But in other situations we take the vector with the largest eigenvalue
	private int getEigenVectorIndex( DoubleMatrix1D evals ) {
		
		// Find a negative if we can
		for( int i=0; i<evals.size(); i++) {
			if( evals.get(i)>0) {
				return i;
			}
		}
		
		int maxIndex = -1;
		double maxValue = 0;
		for( int i=0; i<evals.size(); i++) {
			double val = evals.get(i);
			if (i==0 || val > maxValue) {
				maxValue = val;
				maxIndex = i;
			}
		}
		return maxIndex;
	}
	
	private static void applyFitResultToMark( FitResult fitResult, Mark mark, ImageDimensions sceneDim, double shellRad ) throws PointsFitterException {
		RotationMatrix rotMatrix = new RotationMatrix( fitResult.getRotMatrix() );
		fitResult.setRotMatrix( fitResult.getRotMatrix() );
		
		//System.out.printf("vecDir=%s\n", vecDir);
		Orientation orientation = new OrientationRotationMatrix( rotMatrix ); 
		
		MarkEllipsoid markEll = (MarkEllipsoid) mark;
		markEll.setShellRad( shellRad );
		markEll.setMarksExplicit( fitResult.getCentrePnt(), orientation, new Point3d(fitResult.getRadiusX(),fitResult.getRadiusY(),fitResult.getRadiusZ()) );

		
		BoundingBox bbox = markEll.bboxAllRegions(sceneDim);
		if (bbox.extent().getX()<1||bbox.extent().getY()<1||bbox.extent().getZ()<1) {
			throw new PointsFitterException("Ellipsoid is outside scene");
		}
	}


	public double getMinRadius() {
		return minRadius;
	}

	public void setMinRadius(double minRadius) {
		this.minRadius = minRadius;
	}

	public boolean isSuppressZCovariance() {
		return suppressZCovariance;
	}


	public void setSuppressZCovariance(boolean suppressZCovariance) {
		this.suppressZCovariance = suppressZCovariance;
	}

}
