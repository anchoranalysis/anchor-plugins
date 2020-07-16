/* (C)2020 */
package ch.ethz.biol.cell.mpp.mark.pointsfitter;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.anchor.mpp.bean.points.fitter.PointsFitterException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ConicFitterUtilities {

    public static DoubleMatrix2D matrixLeftDivide(DoubleMatrix2D num, DoubleMatrix2D dem)
            throws PointsFitterException {
        try {
            return new Algebra().inverse(num).zMult(dem, null);
        } catch (IllegalArgumentException e) {
            throw new PointsFitterException(e);
        }
    }
}
