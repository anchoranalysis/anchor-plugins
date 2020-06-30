package ch.ethz.biol.cell.mpp.mark.pointsfitter;

/*
 * #%L
 * anchor-plugin-points
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


import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.anchor.mpp.bean.points.fitter.InsufficientPointsException;
import org.anchoranalysis.anchor.mpp.bean.points.fitter.PointsFitter;
import org.anchoranalysis.anchor.mpp.bean.points.fitter.PointsFitterException;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.NonNegative;
import org.anchoranalysis.core.geometry.Point3f;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.extent.ImageDimensions;

// Reflects points in each axes if all points are within a certain distance from it
public class ReflectInAxesWithinDist extends PointsFitter {
	
	// START BEAN PROPERTIES
	@BeanField
	private PointsFitter pointsFitter;
	
	@BeanField @NonNegative
	private double distanceX = -1;	// Forces user to set a default
	
	@BeanField @NonNegative
	private double distanceY = -1;	// Forces user to set a default
	
	@BeanField @NonNegative
	private double distanceZ = -1;	// Forces user to set a default
	// END BEAN PROPERTIES
	
	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return pointsFitter.isCompatibleWith(testMark);
	}

	@Override
	public void fit(List<Point3f> points, Mark mark, ImageDimensions dim)
			throws PointsFitterException, InsufficientPointsException {

		if (points.size()==0) {
			pointsFitter.fit(points, mark, dim);
		}
		
		double[] arrDistances = new double[]{ distanceX, distanceY, distanceZ };
		
		List<Point3f> pointsCurrent = points;
		Extent extent = dim.getExtnt();
		
		// Try each dimension: x, y, and z respectively
		for( int d=0; d<3; d++) {
			
			// We try the min side and the max side
			for(int side=0; side<1; side++) {
			
				// Are all points within
				boolean insideDist = arePointsWithinDistOfBorder(
					points,
					extent,
					d,
					side==0,
					arrDistances
				);
				if (insideDist) {
					
					pointsCurrent = reflectInDimension(
						pointsCurrent,
						extent,
						d,
						side==0
					);
					
					// It's not allowed be on both sides of the same dimensions
					break;
				}
			}
		}
		
		pointsFitter.fit(pointsCurrent, mark, dim);
	}
	
	private static List<Point3f> reflectInDimension( List<Point3f> pointsIn, Extent extent, int dimension, boolean min ) {
		
		ArrayList<Point3f> pointsOut = new ArrayList<Point3f>();
		
		for( Point3f p : pointsIn ) {
			pointsOut.add(p);
			
			Point3f pDup = new Point3f( p );
			
			if( min) {
				pDup.setValueByDimension(dimension, pDup.getValueByDimension(dimension)*-1 );
			} else {
				float eMax = extent.getValueByDimension(dimension);
				pDup.setValueByDimension(dimension, (2*eMax) - pDup.getValueByDimension(dimension) );
			}
			
			pointsOut.add(pDup);
		}
		return pointsOut;
	}
	

	// Min=true means the lower side,  Min=false means the higher side of the dimension
	private static boolean arePointsWithinDistOfBorder(
		List<Point3f> points,
		Extent extent,
		int dimension,
		boolean min,
		double[] arrDistances
	) {
		
		double dimMax = extent.getValueByDimension(dimension);
		double maxAllowedDist = arrDistances[dimension];
		
		for( Point3f p : points) {

			double pntInDim = p.getValueByDimension(dimension);
			double dist = min ? pntInDim : dimMax - pntInDim;
			
			if( dist>maxAllowedDist) {
				return false;
			}
			
		}
		return true;
	}

	public PointsFitter getPointsFitter() {
		return pointsFitter;
	}

	public void setPointsFitter(PointsFitter pointsFitter) {
		this.pointsFitter = pointsFitter;
	}

	public double getDistanceX() {
		return distanceX;
	}

	public void setDistanceX(double distanceX) {
		this.distanceX = distanceX;
	}

	public double getDistanceY() {
		return distanceY;
	}

	public void setDistanceY(double distanceY) {
		this.distanceY = distanceY;
	}

	public double getDistanceZ() {
		return distanceZ;
	}

	public void setDistanceZ(double distanceZ) {
		this.distanceZ = distanceZ;
	}


}
