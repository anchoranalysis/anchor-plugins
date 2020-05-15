package org.anchoranalysis.plugin.mpp.bean.proposer.bound;

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
import org.anchoranalysis.image.bean.unitvalue.distance.UnitValueDistance;
import org.anchoranalysis.image.bean.unitvalue.distance.UnitValueDistanceVoxels;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.extent.ImageRes;
import org.anchoranalysis.math.rotation.RotationMatrix;

public class CloseFit extends BoundProposer {

	// START BEAN PROPERTIES
	@BeanField
	private BoundProposer boundProposer;
	
	@BeanField
	private UnitValueDistance edgeDevOutside = new UnitValueDistanceVoxels( 8.0 );
	
	@BeanField
	private UnitValueDistance edgeDevInside = new UnitValueDistanceVoxels( 0.0 );
	// END BEAN PROPERTIES
		
	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return boundProposer.isCompatibleWith(testMark);
	}

	@Override
	public BidirectionalBound propose(Point3d pos, RotationMatrix orientation,
			ImageDim bndScene, RslvdBound minMaxBound, ErrorNode proposerFailureDescription) {

		proposerFailureDescription = proposerFailureDescription.add("BoundProposerCloseFit");
		
		// Our delegate bound is what is our normal best guess on the bounds
		BidirectionalBound delegateBound = calcDelegate(pos, orientation, bndScene, minMaxBound, proposerFailureDescription);
		if (delegateBound==null) {
			return null;
		}
		
		proposerFailureDescription.add("delegateBound=" + delegateBound.toString());

		// But we try to get a new better set of bounds, basedup on a 'close fit' criteria
		BidirectionalBound bothNew = proposeBoundCloseFit(pos, bndScene, minMaxBound, delegateBound, proposerFailureDescription );
		
		proposerFailureDescription.add("delegateBoundAfterCloseFit=" + bothNew.toString());
		
		return bothNew;
	}

	public BoundProposer getBoundProposer() {
		return boundProposer;
	}

	public void setBoundProposer(BoundProposer boundProposer) {
		this.boundProposer = boundProposer;
	}

	// We basically restrict the bounds, so that they fall inside a certain distance from the delegateBound minOfMax
	//   but only if the existing bounds aren't at the max
	//
	// We effectively assume with symmetrical bounds around
	private BidirectionalBound proposeBoundCloseFit(Point3d pos, ImageDim bndScene, RslvdBound minMaxBound, BidirectionalBound boundAxis, ErrorNode proposerFailureDescription ) {
		
		/*if (boundAxis.getForward()==null || boundAxis.getReverse()==null) {
			proposerFailureDescription.add("One of the bound axes is null");
			return null;
		}*/
		
		/*double max = boundAxis.getMinOfMax();
		
		RslvdBound bu = new RslvdBound( max - edgeDevInside, max + edgeDevOutside).intersect(minMaxBound);*/
		
		BidirectionalBound bothNew = new BidirectionalBound();
		
		bothNew.setForward( createCloseFitBound(boundAxis.getForward(), minMaxBound, bndScene.getRes()) );
		bothNew.setReverse( createCloseFitBound(boundAxis.getReverse(), minMaxBound, bndScene.getRes()) );
		return bothNew;
	}
	
	// We create the closefit, preserving null if we have no idea where the bound lies
	private RslvdBound createCloseFitBound( RslvdBound exst, RslvdBound minMaxBound, ImageRes res ) {

		double edgeDevInsideRslv = 1;
		double edgeDevOutsideRslv = 1;
		
		if (exst!=null) {
			double max = exst.getMax();
			return new RslvdBound( max - edgeDevInsideRslv, max + edgeDevOutsideRslv).intersect(minMaxBound);
		} else {
			return null;
		}
	}

	
	private BidirectionalBound calcDelegate( Point3d pos, RotationMatrix orientation, ImageDim bndScene, RslvdBound minMaxBound, ErrorNode proposerFailureDescription ) {
		
		BidirectionalBound bothX = getBoundProposer().propose(pos, orientation, bndScene, minMaxBound, proposerFailureDescription);
		
		if (bothX==null) {
			return null;
		}

		return bothX;
	}

	public UnitValueDistance getEdgeDevOutside() {
		return edgeDevOutside;
	}

	public void setEdgeDevOutside(UnitValueDistance edgeDevOutside) {
		this.edgeDevOutside = edgeDevOutside;
	}

	public UnitValueDistance getEdgeDevInside() {
		return edgeDevInside;
	}

	public void setEdgeDevInside(UnitValueDistance edgeDevInside) {
		this.edgeDevInside = edgeDevInside;
	}
}
