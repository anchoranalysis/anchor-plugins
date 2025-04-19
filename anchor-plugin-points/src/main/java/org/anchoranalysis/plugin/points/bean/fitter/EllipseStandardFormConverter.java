/*-
 * #%L
 * anchor-plugin-points
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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

package org.anchoranalysis.plugin.points.bean.fitter;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.linalg.Algebra;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.spatial.point.Point2d;

/**
 * Extracts forms for 'standard form' representation of an ellipse from the coefficients for a 2nd
 * order polynomial describing the ellipse.
 *
 * <p>SEE 'Information About Ellipses' by Eberly (URL below)
 * http://www.geometrictools.com/Documentation/InformationAboutEllipses.pdf
 *
 * <p>See Matlab Prototype (ls_ellipse/convert_to_standard_form.m)
 *
 * <p>The matrix coefficients are as follows (in a vector):
 *
 * <ul>
 *   <li>0 = x^2
 *   <li>1 = xy/2
 *   <li>2 = y^2
 *   <li>3 = x
 *   <li>4 = y
 *   <li>5 = c
 * </ul>
 */
public class EllipseStandardFormConverter {

    /** The matrix of coefficients describing the ellipse. */
    private DoubleMatrix1D matrix;

    /** Center-Point x */
    private double k1;

    /** Center-Point y */
    private double k2;

    /** SEMI-major axis */
    private double axisMajor;

    /** SEMI-minor axis */
    private double axisMinor;

    /** Vector of direction of minor axis */
    private DoubleMatrix1D directionMinor;

    /** Vector of direction of major axis */
    private DoubleMatrix1D directionMajor;

    /**
     * Creates a new {@link EllipseStandardFormConverter} and performs the conversion.
     *
     * @param matrix the matrix of coefficients describing the ellipse
     * @throws CreateException if the matrix does not describe an ellipse
     */
    public EllipseStandardFormConverter(DoubleMatrix1D matrix) throws CreateException {
        this.matrix = matrix;
        convert();
    }

    @SuppressWarnings("static-access")
    private void convert() throws CreateException {

        double a11 = matrix.get(0);
        double a12 = matrix.get(1) / 2;
        double a22 = matrix.get(2);
        double b1 = matrix.get(3);
        double b2 = matrix.get(4);
        double c = matrix.get(5);

        if (a11 * a22 - Math.pow(a12, 2.0) <= 0) {
            throw new CreateException("Not an ellipse");
        }

        // STEP 1 (Center)
        double kdem = 2 * (Math.pow(a12, 2) - (a11 * a22));
        assert (kdem != 0);
        k1 = ((a22 * b1) - (a12 * b2)) / kdem;
        k2 = ((a11 * b2) - (a12 * b1)) / kdem;

        // STEP 2
        double mu =
                1 / ((a11 * Math.pow(k1, 2)) + (2 * a12 * k1 * k2) + (a22 * Math.pow(k2, 2)) - c);
        double m11 = mu * a11;
        double m12 = mu * a12;
        double m22 = mu * a22;

        // STEP 3
        double lambdaCommon = Math.pow(Math.pow(m11 - m22, 2) + (4 * Math.pow(m12, 2)), 0.5);

        double lambda1 = ((m11 + m22) + lambdaCommon) / 2;
        axisMinor = Math.pow(lambda1, -0.5);

        double lambda2 = ((m11 + m22) - lambdaCommon) / 2;
        axisMajor = Math.pow(lambda2, -0.5);

        // Angle with minor axis
        directionMinor = DoubleFactory1D.dense.make(2);
        if (m11 >= m22) {
            directionMinor.set(0, lambda1 - m22);
            directionMinor.set(1, m12);
        } else {
            directionMinor.set(0, m12);
            directionMinor.set(1, lambda1 - m11);
        }

        Algebra a = new Algebra();

        cern.jet.math.Functions F = cern.jet.math.Functions.functions; // NOSONAR

        double directionMinorNorm = Math.pow(a.norm2(directionMinor), 0.5);
        directionMinor.assign(
                F.div(directionMinorNorm) // NOSONAR
                );

        // Angle with major axis
        directionMajor = DoubleFactory1D.dense.make(2);
        directionMajor.set(0, -1 * directionMinor.get(1));
        directionMajor.set(1, directionMinor.get(0));
    }

    /**
     * Gets the x-coordinate of the ellipse's center point.
     *
     * @return the x-coordinate of the center point
     */
    public double getCenterPointX() {
        return k1;
    }

    /**
     * Gets the y-coordinate of the ellipse's center point.
     *
     * @return the y-coordinate of the center point
     */
    public double getCenterPointY() {
        return k2;
    }

    /**
     * Gets the center point of the ellipse.
     *
     * @return a {@link Point2d} representing the center point
     */
    public Point2d centerPoint() {
        return new Point2d(k1, k2);
    }

    /**
     * Gets the length of the semi-major axis.
     *
     * @return the length of the semi-major axis
     */
    public double getSemiMajorAxis() {
        return axisMajor;
    }

    /**
     * Gets the length of the semi-minor axis.
     *
     * @return the length of the semi-minor axis
     */
    public double getSemiMinorAxis() {
        return axisMinor;
    }

    /**
     * Gets the angle of the major axis in radians.
     *
     * @return the angle of the major axis
     */
    public double getMajorAxisAngle() {
        return atanHandlingNan(getMajorAxisSlope());
    }

    /**
     * Gets the angle of the minor axis in radians.
     *
     * @return the angle of the minor axis
     */
    public double getMinorAxisAngle() {
        return atanHandlingNan(getMajorAxisSlope());
    }

    /**
     * Calculates the arctangent, handling NaN values.
     *
     * @param val the value to calculate arctangent for
     * @return the arctangent in radians, or PI/2 if the input is NaN
     */
    public static double atanHandlingNan(double val) {
        // If val is NAN then we return PI/2
        if (Double.isNaN(val)) {
            return (Math.PI / 2);
        } else {
            return Math.atan(val);
        }
    }

    /**
     * Gets the slope of the major axis.
     *
     * @return the slope of the major axis
     */
    public double getMajorAxisSlope() {
        return directionMajor.get(1) / directionMajor.get(0);
    }

    /**
     * Gets the slope of the minor axis.
     *
     * @return the slope of the minor axis
     */
    public double getMinorAxisSlope() {
        return directionMinor.get(1) / directionMinor.get(0);
    }
}
