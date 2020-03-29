package org.anchoranalysis.plugin.mpp.bean.proposer.bound;

import org.anchoranalysis.anchor.mpp.bean.bound.BoundCalculator;
import org.anchoranalysis.anchor.mpp.bean.bound.RslvdBound;
import org.anchoranalysis.anchor.mpp.bean.proposer.BoundProposer;
import org.anchoranalysis.anchor.mpp.bound.BidirectionalBound;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.proposer.error.ErrorNode;

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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.math.rotation.RotationMatrix;

public class Simple extends BoundProposer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6382232253095160193L;

	// START BEAN PROPERTIES
	@BeanField
	private BoundCalculator boundCalculator = null;
	// END BEAN PROPERTIES
	
	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return true;
	}

	@Override
	public BidirectionalBound propose( Point3d pos, RotationMatrix orientation, ImageDim bndScene, RslvdBound minMaxBound, ErrorNode proposerFailureDescription ) {
		
		proposerFailureDescription = proposerFailureDescription.add("BoundProposerSimple");
		
		// TODO, this should not occur in normal mode
		// We associate a mark to describe the position and orientation
//		MarkEllipse associatedMark = new MarkEllipse();
//		associatedMark.setMarksExplicit(pos, orientation, new Point2d(10,4) );
//		
//		proposerFailureDescription.add( String.format("pos=%s", pos.toString() ), associatedMark );
//		proposerFailureDescription.add( String.format("orientation=%s", orientation.toString() ), associatedMark );
		
		// We get better bounds for the particular rotation in the X-Direction
		BidirectionalBound bothX = boundCalculator.calcBound(pos, orientation, proposerFailureDescription );
		
		// We replace any nulls with our general bound
		
		if (bothX==null) {
			//proposerFailureDescription.add( String.format("cannot calculate bound in direction %f", orientation2D.getAngleRadians()) );
			proposerFailureDescription.addFormatted("cannot calculate bound in direction " + orientation.toString());
			return null;
		}
		
		/*{
			if (bothX.getForward()==null) {
				bothX.setForward( minMaxBound );
			}
			
			if (bothX.getReverse()==null) {
				bothX.setReverse( minMaxBound );
			}
		}*/
		return bothX;
	}
	
	public BoundCalculator getBoundCalculator() {
		return boundCalculator;
	}

	public void setBoundCalculator(BoundCalculator boundCalculator) {
		this.boundCalculator = boundCalculator;
	}

	
}
