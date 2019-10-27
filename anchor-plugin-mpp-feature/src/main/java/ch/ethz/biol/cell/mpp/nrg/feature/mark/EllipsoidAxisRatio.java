package ch.ethz.biol.cell.mpp.nrg.feature.mark;

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

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Vector3d;
import org.anchoranalysis.feature.cache.CacheSession;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.init.FeatureInitParams;
import org.anchoranalysis.image.bean.orientation.DirectionVectorBean;
import org.anchoranalysis.image.orientation.DirectionVector;
import org.anchoranalysis.image.orientation.Orientation;
import org.anchoranalysis.math.equation.QuadraticEquationSolver;
import org.anchoranalysis.math.equation.QuadraticEquationSolver.QuadraticRoots;
import org.anchoranalysis.math.rotation.RotationMatrix;

import ch.ethz.biol.cell.mpp.mark.MarkEllipsoid;

// Computes the axis ratio of the ellipse formed by a plane of an orientation relative to the ellipsoid
//   intersectiong with the ellipsoid.  This is constant for all parallel planes.
//
// See paper:  Colin C. Ferguson "Intersections of Ellipsoids and Planes of Arbitrary Orientation and Position
//
public class EllipsoidAxisRatio extends FeatureMark {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private DirectionVectorBean directionVector;
	// END BEAN PROPERTIES

	private DirectionVector dv;
	
	@Override
	public void beforeCalc(FeatureInitParams params, CacheSession cache) throws InitException {
		super.beforeCalc(params, cache);
		dv = directionVector.createVector();
	}

	@Override
	public double calc(FeatureMarkParams params) throws FeatureCalcException {
		
		if (!(params.getMark() instanceof MarkEllipsoid)) {
			throw new FeatureCalcException("Only supports MarkEllipsoids");
		}
		
		MarkEllipsoid mark = (MarkEllipsoid) params.getMark();
		
		Orientation orientation = mark.getOrientation();
		RotationMatrix rotMatrix = orientation.createRotationMatrix().transpose();
		
		double[] radii  =mark.createRadiiArray();
		//
		double a_1 = Math.pow(radii[0], -2);
		double a_2 = Math.pow(radii[1], -2);
		double a_3 = Math.pow(radii[2], -2);
		
		Vector3d normalToPlane = dv.createVector3d();
		normalToPlane.normalize();
		Point3d beta = rotMatrix.calcRotatedPoint( new Point3d(normalToPlane) );
		
		double beta_1 = beta.getX();
		double beta_2 = beta.getY();
		double beta_3 = beta.getZ();
		
		double beta_1_sq = Math.pow(beta_1, 2);
		double beta_2_sq = Math.pow(beta_2, 2);
		double beta_3_sq = Math.pow(beta_3, 2);
		
		
		
		double eq_xsquared = (a_2 * a_3 * beta_1_sq) + (a_1 * a_3 * beta_2_sq )+ (a_1 * a_2 * beta_3_sq);
		double eq_x = -1 * (((a_2 + a_3) * beta_1_sq) + ((a_1 + a_3) * beta_2_sq) + ((a_1 + a_2) * beta_3_sq));
		double eq = 1;
		
		// Now we get
		QuadraticRoots roots;
		try {
			roots = QuadraticEquationSolver.solveQuadraticEquation(eq_xsquared, eq_x, eq);
		} catch (OperationFailedException e) {
			throw new FeatureCalcException(e);
		}
			
		double r1 = Math.sqrt( roots.getRoot1() );
		double r2 = Math.sqrt( roots.getRoot2() );
		
		double major = Math.max(r1, r2);
		double minor = Math.min(r1, r2);
		
		return major/minor;
	}

	public DirectionVectorBean getDirectionVector() {
		return directionVector;
	}

	public void setDirectionVector(DirectionVectorBean directionVector) {
		this.directionVector = directionVector;
	}

}
