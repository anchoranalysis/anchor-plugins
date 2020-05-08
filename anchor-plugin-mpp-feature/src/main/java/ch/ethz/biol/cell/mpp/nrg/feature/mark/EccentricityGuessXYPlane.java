package ch.ethz.biol.cell.mpp.nrg.feature.mark;

import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureMark;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureInputMark;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.MarkAbstractRadii;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipsoid;

/*-
 * #%L
 * anchor-plugin-mpp-feature
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

import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Vector3d;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.orientation.Orientation;
import org.anchoranalysis.math.rotation.RotationMatrix;

// Calculates the eccentricity of the ellipse by considering the two planes which are furtherest way from the Z unit-vector
// If it's an ellipsoid it calculates the Meridional Eccentricity i.e. the eccentricity
//    of the ellipse that cuts across the plane formed by the longest and shortest axes
public class EccentricityGuessXYPlane extends FeatureMark {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private double calcEccentricity( double semiMajorAxis, double semiMinorAxis ) {
		double ratio = semiMinorAxis/semiMajorAxis;
		return Math.sqrt( 1.0 - Math.pow(ratio, 2.0) );
	}

	//	angle between vectors (we don't bother with the arccos), as we are just finding the minimum anyway  
	private double cosAngleBetweenVectors( Vector3d vec1, Vector3d vec2 ) {
		double num = vec1.dot(vec2);
		double dem = vec1.length() * vec2.length();
		return num/dem;
	}
	
	
	@Override
	public double calc(SessionInput<FeatureInputMark> input) throws FeatureCalcException {

		Mark mark = input.get().getMark();
		
		if (mark instanceof MarkAbstractRadii) {
			MarkEllipsoid markCast = (MarkEllipsoid) mark;
			
			

			
			// Old style
			// return calcEccentricity(radii[radii.length-1], radii[0]);
			double[] radiiOrdered = markCast.radiiOrdered();
			if (radiiOrdered.length==2) {
				
				return calcEccentricity(radiiOrdered[1], radiiOrdered[0]);
			} else {
				
				double[] radii = markCast.createRadiiArray();
				
				Orientation orientation = markCast.getOrientation();
				RotationMatrix rotMatrix = orientation.createRotationMatrix();
				
				Vector3d xRot = new Vector3d( rotMatrix.calcRotatedPoint( new Point3d(1,0,0) ) );
				Vector3d yRot = new Vector3d( rotMatrix.calcRotatedPoint( new Point3d(0,1,0) ) );
				Vector3d zRot = new Vector3d( rotMatrix.calcRotatedPoint( new Point3d(0,0,1) ) );
				
				Vector3d unitVectorZ = new Vector3d(0,0,1.0);
				Vector3d unitVectorZRev = new Vector3d(0,0,1.0);
				
				double xAngle = Math.min( cosAngleBetweenVectors(xRot,unitVectorZ),cosAngleBetweenVectors(xRot,unitVectorZRev) );
				double yAngle = Math.min( cosAngleBetweenVectors(yRot,unitVectorZ),cosAngleBetweenVectors(yRot,unitVectorZRev) );
				double zAngle = Math.min( cosAngleBetweenVectors(zRot,unitVectorZ),cosAngleBetweenVectors(zRot,unitVectorZRev) );
				
				
				//double ecc10 =  calcEccentricity(radiiOrdered[1],radiiOrdered[0]);
				//double ecc20 =  calcEccentricity(radiiOrdered[2],radiiOrdered[0]);
				//double ecc21 =  calcEccentricity(radiiOrdered[2],radiiOrdered[1]);
				
				//System.out.printf("Eccentricities: %f, %f, %f\n", ecc10, ecc20, ecc21 );
				//System.out.printf("Angle between vectors: %f, %f, %f\n", Math.acos(xAngle), Math.acos(yAngle), Math.acos(zAngle) );
				
				double radiiOther1;
				double radiiOther2;
				// Find smallest angle, and select the other two radii for our eccentricty
				if (xAngle > yAngle) {
					if (xAngle > zAngle) {
						// x is the smallest
						//System.out.printf("x is smallest\n");
						radiiOther1 = radii[1];
						radiiOther2 = radii[2];
					} else {
						//System.out.printf("z is smallest\n");
						// z is the smallest
						radiiOther1 = radii[0];
						radiiOther2 = radii[1];						
					}
				} else {
					if (yAngle > zAngle) {
						//System.out.printf("y is smallest\n");
						// y is the smallest
						radiiOther1 = radii[0];
						radiiOther2 = radii[2];
					} else {
						//System.out.printf("z is smallest\n");
						// z is the smallest
						radiiOther1 = radii[0];
						radiiOther2 = radii[1];
					}
				}
				
				double major = Math.max(radiiOther1, radiiOther2);
				double minor = Math.min(radiiOther1, radiiOther2);
				
				return calcEccentricity(major,minor);
			}

		} else {
			throw new FeatureCalcException("mark must be of type MarkAbstractRadii");
		}

		
	}

}
