package ch.ethz.biol.cell.mpp.nrg.feature.mark;

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

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Vector3d;
import org.anchoranalysis.feature.cache.CacheSession;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.init.FeatureInitParams;
import org.anchoranalysis.image.bean.orientation.DirectionVectorBean;
import org.anchoranalysis.image.orientation.DirectionVector;
import org.anchoranalysis.image.orientation.Orientation;
import org.anchoranalysis.math.rotation.RotationMatrix;

// Computes the axis ratio of the ellipse formed by a plane of an orientation relative to the ellipsoid
//   intersectiong with the ellipsoid.  This is constant for all parallel planes.
//
// See paper:  Colin C. Ferguson "Intersections of Ellipsoids and Planes of Arbitrary Orientation and Position
//
public class EllipsoidMaxAreaIntersectingPlane extends FeatureMark {
	
	
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
		
		Vector3d normalToPlane = dv.createVector3d();
		normalToPlane.normalize();
		Point3d beta = rotMatrix.calcRotatedPoint( new Point3d(normalToPlane) );
		
		double beta_1 = beta.getX();
		double beta_2 = beta.getY();
		double beta_3 = beta.getZ();
		
		double beta_1_sq = Math.pow(beta_1, 2);
		double beta_2_sq = Math.pow(beta_2, 2);
		double beta_3_sq = Math.pow(beta_3, 2);
		
		
		double P_t = Math.sqrt((Math.pow(radii[0],2) * beta_1_sq) + (Math.pow(radii[1],2) * beta_2_sq) + (Math.pow(radii[2],2) * beta_3_sq)); 

		double area_center = (Math.PI * radii[0] * radii[1] * radii[2]) / P_t;
		
		return area_center;
	}

	public DirectionVectorBean getDirectionVector() {
		return directionVector;
	}

	public void setDirectionVector(DirectionVectorBean directionVector) {
		this.directionVector = directionVector;
	}

}
