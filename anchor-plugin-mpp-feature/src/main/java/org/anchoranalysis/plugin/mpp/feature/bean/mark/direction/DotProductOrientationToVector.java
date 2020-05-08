package org.anchoranalysis.plugin.mpp.feature.bean.mark.direction;

import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipsoid;

/*
 * #%L
 * anchor-plugin-mpp-feature
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


import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Vector3d;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.orientation.Orientation;
import org.anchoranalysis.math.rotation.RotationMatrix;

// Considers the 3 directions of an Ellipsoid
public class DotProductOrientationToVector extends FeatureMarkDirection {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected double calcForEllipsoid(MarkEllipsoid mark, Orientation orientation, RotationMatrix rotMatrix, Vector3d directionVector)
			throws FeatureCalcException {

		double minDot = Double.POSITIVE_INFINITY;
		
		for( int d=0; d<3; d++ ) {
			Point3d vec = rotMatrix.column(d);
			
			double dot = Math.acos(
				directionVector.dot(vec)
			);
			
			if (dot<minDot) {
				minDot = dot;
			}
		}
		
		return minDot;
	}
}
