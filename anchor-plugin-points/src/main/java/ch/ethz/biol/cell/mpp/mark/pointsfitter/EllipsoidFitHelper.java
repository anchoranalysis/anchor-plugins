package ch.ethz.biol.cell.mpp.mark.pointsfitter;

import org.anchoranalysis.anchor.mpp.bean.points.fitter.PointsFitterException;
import org.anchoranalysis.core.geometry.Point3d;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.EigenvalueDecomposition;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
public class EllipsoidFitHelper {

	public static FitResult createFitResultFromMatrixAandCenter( DoubleMatrix2D matrixA, DoubleMatrix2D matrixCenter, boolean suppressZCovariance ) throws PointsFitterException {

		DoubleMatrix2D matrixR = createMatrixR( matrixA, matrixCenter  );
		
		FitResult fitResult = new FitResult();
		
		setRadiiFromDecomposition(
			fitResult,
			createMatrixE(matrixR,suppressZCovariance)
		);
		setCenterFromFirstColumn(fitResult, matrixCenter);
			
		assert( !Double.isNaN(fitResult.getRadiusX()) );
		assert( !Double.isNaN(fitResult.getRadiusY()) );
		assert( !Double.isNaN(fitResult.getRadiusZ()) );
		assert( fitResult.getRadiusX() > 0 );
		assert( fitResult.getRadiusY() > 0 );
		assert( fitResult.getRadiusZ() > 0 );
		
		return fitResult;
	}
	
	private static void setCenterFromFirstColumn( FitResult fitResult, DoubleMatrix2D matrixCenter ) {
		fitResult.setCenterPoint(
			new Point3d(
				matrixCenter.get(0, 0),
				matrixCenter.get(1, 0),
				matrixCenter.get(2, 0)
			)
		);
	}

	private static DoubleMatrix2D createMatrixE(DoubleMatrix2D matrixR, boolean suppressZCovariance) {
		DoubleMatrix2D e = matrixR.viewPart(0, 0, 3, 3);
		
		double divVal = matrixR.get(3, 3) * -1;
		
		e.assign( cern.jet.math.Functions.div(divVal) );
		
		if (suppressZCovariance) {
			e.set(2, 0, 0);
			e.set(2, 1, 0);
			e.set(0, 2, 0);
			e.set(1, 2, 0);
		}
		return e;
	}
	
	private static void setRadiiFromDecomposition(FitResult fitResult, DoubleMatrix2D e) throws PointsFitterException {
		try {
			EigenvalueDecomposition evd = new EigenvalueDecomposition( e );
			
			fitResult.setRadiusX( radiusFromDiagonal(evd,0) );
			fitResult.setRadiusY( radiusFromDiagonal(evd,1) );
			fitResult.setRadiusZ( radiusFromDiagonal(evd,2) );

			fitResult.setRotMatrix( evd.getV() );		

		} catch (ArrayIndexOutOfBoundsException e1) {
			throw new PointsFitterException( String.format("Cannot init EigenValueDecomposition with arg='%s'",e.toString()));
		}
	}
	
	private static double radiusFromDiagonal(EigenvalueDecomposition evd, int index) {
		double valFromDiagonal = evd.getD().get(index, index);
		return Math.sqrt( 1/ Math.abs(valFromDiagonal));
	}
	
	private static DoubleMatrix2D createMatrixR( DoubleMatrix2D matrixA, DoubleMatrix2D matrixCenter ) {
		
		DoubleMatrix2D matrixT = DoubleFactory2D.dense.identity(4);
		
		matrixT.set(3, 0, matrixCenter.get(0, 0) );
		matrixT.set(3, 1, matrixCenter.get(1, 0) );
		matrixT.set(3, 2, matrixCenter.get(2, 0) );
		
		return matrixT.zMult(matrixA, null).zMult(matrixT.viewDice(), null);
	}
}
