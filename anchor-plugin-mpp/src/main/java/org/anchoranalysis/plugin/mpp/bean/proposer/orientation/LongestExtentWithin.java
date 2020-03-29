package org.anchoranalysis.plugin.mpp.bean.proposer.orientation;

/*
 * #%L
 * anchor-plugin-mpp
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

import org.anchoranalysis.anchor.mpp.bean.bound.BoundCalculator;
import org.anchoranalysis.anchor.mpp.bean.bound.RslvdBound;
import org.anchoranalysis.anchor.mpp.bean.proposer.OrientationProposer;
import org.anchoranalysis.anchor.mpp.bound.BidirectionalBound;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.MarkAbstractPosition;
import org.anchoranalysis.anchor.mpp.proposer.error.ErrorNode;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.orientation.Orientation;
import org.anchoranalysis.image.orientation.Orientation2D;
import org.anchoranalysis.image.orientation.Orientation3DEulerAngles;

// Gets the longest extent within a certain ratio between the bounds,
//   and below the upper maximum
public class LongestExtentWithin extends OrientationProposer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7806665021959710908L;

	// START BEAN
	@BeanField
	private double incrementDegrees = 1;
	
	@BeanField
	private double boundsRatio = 1.1;
	
	@BeanField
	private BoundCalculator boundCalculator;

	@BeanField
	private boolean rotateOnlyIn2DPlane = false;
	// END BEAN
	
	private class OrientationList {
		
		ArrayList<Orientation> listOrientationsWithinBoundsRatio = new ArrayList<>();
		ArrayList<Orientation> listOrientationsUnbounded = new ArrayList<>();
		
		public void addOrientationIfUseful( Orientation orientation, Mark mark, RslvdBound minMaxBound, ImageDim dim, ErrorNode errorNode ) {
			
			BidirectionalBound bib = boundCalculator.calcBound( mark.centerPoint(), orientation.createRotationMatrix(), errorNode);
			
			if (bib==null) {
				errorNode.add("null bound");
				return;
			}
			
			if (bib.isUnboundedAtBothEnds()) {
				errorNode.add("unbounded at both ends");
				return;
			}
			
			if (bib.isUnbounded()) {
				
				double max = bib.getMaxOfMax();
				if (max < minMaxBound.getMax()) {
					listOrientationsUnbounded.add(orientation);
				} else {
					errorNode.addFormatted("unbounded is above marks bound max (%f > %f)", max, minMaxBound.getMax());
				}
			}
			
			double rb = bib.ratioBounds(dim); 
			//System.out.printf("bounds reatio=%f\n",rb);
			
			if (rb > boundsRatio) {
				errorNode.addFormatted("outside bounds ratio (%f br=%f)",rb, boundsRatio);
				return;
			}
			
			double max = bib.getMaxOfMax();
			
			if (max > minMaxBound.getMax()) {
				errorNode.addFormatted("above marks bound max (%f > %f)",max, minMaxBound.getMax());
				return;
			}
			
			listOrientationsWithinBoundsRatio.add(orientation);
			
			// We disconsider the max, if it is outside our mark
			//System.out.printf("ADDING bounds ratio=%f\n",rb);
			// We record it as a possible value, and we pick from the arraylist at the end
		}

		
		// We adopt the following priority
		//		If there are orientations within the Bounds Ratio, WE SAMPLE UNIFORMLY FROM THEM
		//		If not, and there are unbounded orientations, WE SAMPLE UNIFORMLY FROM THEM
		public Orientation sample( RandomNumberGenerator re ) {

			if (listOrientationsWithinBoundsRatio.size()>0) {
				return listOrientationsWithinBoundsRatio.get( (int) (re.nextDouble() * listOrientationsWithinBoundsRatio.size()) );
			} else if (listOrientationsUnbounded.size()>0) {
				return listOrientationsUnbounded.get( (int) (re.nextDouble() * listOrientationsUnbounded.size()) );
			} else {
				return null;
			}
		}
	}
	
	private OrientationList findAllOrientations2D( Mark mark, RslvdBound minMaxBound, ImageDim dim, ErrorNode errorNode ) {
		
		//System.out.println("NEW FIND ALL ORIENTATIONS 2D");
		
		double incrementRadians = (incrementDegrees / 180) * Math.PI;
		
		OrientationList listOrientations = new OrientationList();
		
		// We loop through every positive angle and pick the one with the greatest extent
		for (double angle=0; angle < Math.PI; angle += incrementRadians) {
		
			Orientation orientation = new Orientation2D(angle);
			
			listOrientations.addOrientationIfUseful(orientation, mark, minMaxBound, dim, errorNode);

		}
		
		return listOrientations;
	}
	
	
	private OrientationList findAllOrientations3D( Mark mark, RslvdBound minMaxBound, ImageDim dim, ErrorNode errorNode ) {
		
		double incrementRadians = (incrementDegrees / 180) * Math.PI;
		
		OrientationList listOrientations = new OrientationList();
		
		// We loop through every positive angle and pick the one with the greatest extent
		for (double x=0; x < Math.PI; x += incrementRadians) {
			for (double y=0; y < Math.PI; y += incrementRadians) {
				for (double z=0; z < Math.PI; z += incrementRadians) {
				
					Orientation3DEulerAngles orientation = new Orientation3DEulerAngles();
					orientation.setRotXRadians(x);
					orientation.setRotYRadians(y);
					orientation.setRotZRadians(z);
					
					listOrientations.addOrientationIfUseful(orientation, mark, minMaxBound, dim, errorNode);
				}
			}
		}
		
		return listOrientations;
	}
	
	
	private OrientationList findAllOrientations( Mark mark, RslvdBound minMaxBound, ImageDim dim, ErrorNode errorNode ) {
		
		if (dim.getZ()>1 && !rotateOnlyIn2DPlane) {
			return findAllOrientations3D(mark, minMaxBound, dim, errorNode);
		} else {
			return findAllOrientations2D(mark, minMaxBound, dim, errorNode);
		}
	}
	
	
	@Override
	public Orientation propose(Mark mark, ImageDim dim, RandomNumberGenerator re, ErrorNode errorNode ) {
		
		errorNode = errorNode.add("LongestExtentWithin");
		
		try {
			RslvdBound minMaxBound = getSharedObjects().getMarkBounds().calcMinMax(dim.getRes(), dim.getZ()>1 );
			
			OrientationList listOrientations = findAllOrientations( mark, minMaxBound, dim, errorNode);
			return listOrientations.sample(re);
			
		} catch (GetOperationFailedException e) {
			errorNode.add(e);
			return null;
		}
		
		/*if (maxExtent!=-1) {
			return new Orientation2D( angleAtMax );	
		} else {
			return null;
		}*/
	}

	public double getIncrementDegrees() {
		return incrementDegrees;
	}

	public void setIncrementDegrees(double incrementDegrees) {
		this.incrementDegrees = incrementDegrees;
	}

	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return testMark instanceof MarkAbstractPosition;
	}

	public BoundCalculator getBoundCalculator() {
		return boundCalculator;
	}

	public void setBoundCalculator(BoundCalculator boundCalculator) {
		this.boundCalculator = boundCalculator;
	}

	public double getBoundsRatio() {
		return boundsRatio;
	}

	public void setBoundsRatio(double boundsRatio) {
		this.boundsRatio = boundsRatio;
	}

	public boolean isRotateOnlyIn2DPlane() {
		return rotateOnlyIn2DPlane;
	}

	public void setRotateOnlyIn2DPlane(boolean rotateOnlyIn2DPlane) {
		this.rotateOnlyIn2DPlane = rotateOnlyIn2DPlane;
	}



}
