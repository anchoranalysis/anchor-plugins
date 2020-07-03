package ch.ethz.biol.cell.mpp.mark.pointsfitter;

/*-
 * #%L
 * anchor-plugin-points
 * %%
 * Copyright (C) 2010 - 2019 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point2d;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.linalg.Algebra;

//
// Extracts forms for 'standard form' representation of an ellipse
//   from the coefficients for a 2nd order polynomial describing the ellipse
//
// SEE 'Information About Ellipses' by Eberly (URL below)
// http://www.geometrictools.com/Documentation/InformationAboutEllipses.pdf
//
// See Matlab Prototype (ls_ellipse/convert_to_standard_form.m)
//
//  The matrix coefficients are as follows (in a vector)
//    0 = x^2
//    1 = xy/2
//    2 = y^2
//    3 = x
//    4 = y
//    5 = c 
//
//
public class EllipseStandardFormConverter {

	private DoubleMatrix1D matrix;
	
	private double k1;		// Centre-Point x
	private double k2;		// Centre-Point y
	private double axis_major;	// SEMI-major axis
	private double axis_minor;	// SEMI-minor axis
	
	private DoubleMatrix1D U1;	// Vector of direction of minor axis
	private DoubleMatrix1D U2;	// Vector of direction of major axis

	public EllipseStandardFormConverter( DoubleMatrix1D matrix ) {
		this.matrix = matrix;
	}
	
	@SuppressWarnings("static-access")
	public void convert() throws OperationFailedException {
		
		double a11 = matrix.get(0);
	    double a12 = matrix.get(1) / 2;
	    double a22 = matrix.get(2);
	    double b1 = matrix.get(3);
	    double b2 = matrix.get(4);
	    double c = matrix.get(5);
	    
	    if (a11 * a22 - Math.pow(a12,2.0)<=0 ) {
	    	throw new OperationFailedException("Not an ellipse");
	    }
	    
	    // STEP 1 (Centre)
	    double kdem = 2 * ( Math.pow(a12,2) - (a11 * a22) );
	    assert( kdem!=0 );
	    k1 = ((a22 * b1)  - (a12 * b2)) / kdem;
	    k2 = ((a11 * b2)  - (a12 * b1)) / kdem;
	    
	    // STEP 2
	    double mu = 1/((a11 * Math.pow(k1,2)) + (2*a12*k1*k2) + (a22*Math.pow(k2,2)) - c);
	    double m11 = mu * a11;
	    double m12 = mu * a12;
	    double m22 = mu * a22;
	    
	    // STEP 3
	    double lambda_common = Math.pow( Math.pow(m11-m22,2) + (4*Math.pow(m12,2) ), 0.5);

	    double lambda1 = ( (m11+m22) + lambda_common)/2;
	    axis_minor =  Math.pow(lambda1,-0.5);
	    
	    double lambda2 = ( (m11+m22) - lambda_common)/2;
	    axis_major =  Math.pow(lambda2,-0.5);
	    
	    // Angle with minor axis
	    U1 = DoubleFactory1D.dense.make(2);
	    if (m11>=m22) {
	        U1.set(0, lambda1-m22);
	        U1.set(1, m12);
	    } else {
	        U1.set(0, m12);
	        U1.set(1, lambda1-m11);
	    }
	    
	    
	    Algebra a = new Algebra();
	    
	    cern.jet.math.Functions F = cern.jet.math.Functions.functions;
	    
	    double U1_norm = Math.pow( a.norm2(U1), 0.5);
	    U1.assign( F.div(U1_norm) );		// NOSONAR

	    // Angle with major axis
	    U2 = DoubleFactory1D.dense.make(2);
	    U2.set(0, -1 * U1.get(1) );
	    U2.set(1, U1.get(0) );
	}

	public double getCenterPointX() {
		return k1;
	}

	public double getCenterPointY() {
		return k2;
	}
	
	public Point2d centerPoint() {
		return new Point2d(k1,k2);
	}
	
	public double getSemiMajorAxis() {
		return axis_major;
	}

	public double getSemiMinorAxis() {
		return axis_minor;
	}
	
	public double getMajorAxisAngle() {
		return atanHandlingNan( getMajorAxisSlope() );
	}
	
	public double getMinorAxisAngle() {
		return atanHandlingNan( getMajorAxisSlope() );
	}
	
	public static double atanHandlingNan( double val ) {
		// If val is NAN then we return PI/2
		if (Double.isNaN(val)) {
			return (Math.PI/2);
		} else {
			return Math.atan( val );
		}
	}
	
	public double getMajorAxisSlope() {
		return U2.get(1) / U2.get(0);
	}
	
	public double getMinorAxisSlope() {
		return U1.get(1) / U1.get(0);
	}
}
