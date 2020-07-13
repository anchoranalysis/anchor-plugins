package ch.ethz.biol.cell.mpp.mark.pointsfitter;

import org.anchoranalysis.anchor.mpp.bean.points.fitter.PointsFitterException;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
class ConicFitterUtilities {

	public static DoubleMatrix2D matrixLeftDivide( DoubleMatrix2D num, DoubleMatrix2D dem ) throws PointsFitterException {
		try {
			return new Algebra().inverse(num).zMult(dem, null);
		} catch (IllegalArgumentException e) {
			throw new PointsFitterException(e);
		}
	}
}
