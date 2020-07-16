/* (C)2020 */
package ch.ethz.biol.cell.mpp.mark.pointsfitter;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;
import java.util.List;
import org.anchoranalysis.anchor.mpp.bean.points.fitter.PointsFitterException;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.core.geometry.Point3f;
import org.anchoranalysis.image.extent.ImageDimensions;

public abstract class LinearLeastSquaresViaNormalEquationBase extends ConicFitterBase {

    @Override
    public void fit(List<Point3f> points, Mark mark, ImageDimensions dimensions)
            throws PointsFitterException {

        if (points.size() < minNumPoints()) {
            throw new PointsFitterException(
                    String.format("Must have at least %d points to fit", minNumPoints()));
        }

        DoubleMatrix2D coefficients = solveNormalEquation(createDesignMatrix(points));
        applyCoefficientsToMark(coefficients, mark, dimensions);
    }

    protected abstract int minNumPoints();

    protected abstract void applyCoefficientsToMark(
            DoubleMatrix2D matrixV, Mark mark, ImageDimensions dimensions)
            throws PointsFitterException;

    protected abstract DoubleMatrix2D createDesignMatrix(List<Point3f> points);

    /**
     * Solves the normal-equation for a Linear Least Squares system.
     *
     * <p>All our dependent variables are treated as 1 (solution to the ellipsoid matrix equation).
     *
     * @param matrixD
     * @return a matrix of optimal coefficients
     * @throws PointsFitterException
     */
    private static DoubleMatrix2D solveNormalEquation(DoubleMatrix2D matrixD)
            throws PointsFitterException {

        DoubleMatrix2D ones = DoubleFactory2D.dense.make(matrixD.rows(), 1, 1);

        return ConicFitterUtilities.matrixLeftDivide(
                matrixD.viewDice().zMult(matrixD, null), matrixD.viewDice().zMult(ones, null));
    }
}
