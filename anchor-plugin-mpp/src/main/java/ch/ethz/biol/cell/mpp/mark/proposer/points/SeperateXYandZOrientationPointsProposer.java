package ch.ethz.biol.cell.mpp.mark.proposer.points;

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
import java.util.List;

import org.anchoranalysis.anchor.mpp.bean.proposer.OrientationProposer;
import org.anchoranalysis.anchor.mpp.bean.proposer.PointsProposer;
import org.anchoranalysis.anchor.mpp.proposer.error.ErrorNode;
import org.anchoranalysis.anchor.mpp.proposer.visualization.CreateProposeVisualizationList;
import org.anchoranalysis.anchor.mpp.proposer.visualization.ICreateProposalVisualization;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.orientation.Orientation;
import org.anchoranalysis.image.orientation.OrientationRotationMatrix;
import org.anchoranalysis.math.rotation.RotationMatrix;
import org.anchoranalysis.math.rotation.RotationMatrix2DFromRadianCreator;

import ch.ethz.biol.cell.imageprocessing.bound.BidirectionalBound;
import ch.ethz.biol.cell.mpp.bound.BoundCalculator;
import ch.ethz.biol.cell.mpp.mark.Mark;
import ch.ethz.biol.cell.mpp.mark.ellipsoidfitter.outlinepixelsretriever.TraverseOutlineException;
import ch.ethz.biol.cell.mpp.mark.proposer.pointsfromorientation.PointsFromOrientationProposer;

public class SeperateXYandZOrientationPointsProposer extends PointsProposer {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private OrientationProposer orientationXYProposer;		// Should find an orientation in the XY plane
	
	@BeanField
	private PointsFromOrientationProposer pointsFromOrientationXYProposer;
	
	@BeanField
	private PointsFromOrientationProposer pointsFromOrientationXYUnboundedProposer;
	
	@BeanField
	private BoundCalculator boundCalculator;
	
	@BeanField
	private double boundsRatio = 1000;	// Effectively an infinity value that includes all feasible possibilities
	// END BEAN PROPERTIES

	// This is a hack to make it easier to do visualization we duplicate each of these
	private PointsFromOrientationProposer pointsFromOrientationXYProposerSecondUse;
	private PointsFromOrientationProposer pointsFromOrientationXYUnboundedProposerSecondUse;

	@Override
	public void onInit() throws InitException {
		super.onInit();
	
		pointsFromOrientationXYProposerSecondUse = pointsFromOrientationXYProposer.duplicateBean();
		pointsFromOrientationXYUnboundedProposerSecondUse = pointsFromOrientationXYUnboundedProposer.duplicateBean();
		
		pointsFromOrientationXYProposerSecondUse.initRecursive(getLogger());
		pointsFromOrientationXYUnboundedProposerSecondUse.initRecursive(getLogger());

	}
	

	private Orientation calculate90degreeOrientation( Orientation orientation ) {
		
		// We rotate
		RotationMatrix orientationMatrix = orientation.createRotationMatrix();
		RotationMatrix rot = new RotationMatrix2DFromRadianCreator(Math.PI/2).createRotationMatrix();
		
		orientationMatrix = orientationMatrix.mult(rot);
		
		return new OrientationRotationMatrix(orientationMatrix);
	}
	
	@Override
	public List<Point3i> propose(Point3d pnt, Mark mark, ImageDim dim, RandomNumberGenerator re, ErrorNode errorNode) {
		
		pointsFromOrientationXYProposer.clearVisualizationState();
		pointsFromOrientationXYUnboundedProposer.clearVisualizationState();
		pointsFromOrientationXYProposerSecondUse.clearVisualizationState();
		pointsFromOrientationXYUnboundedProposerSecondUse.clearVisualizationState();
		
		
		
		Orientation orientation;
		
		Point3d pntRoot = new Point3d(pnt);

		orientation = orientationXYProposer.propose(mark, dim, re, errorNode.add("orientationProposer") );
		
		if (orientation==null) {
			return null;
		}

		Orientation orientation90 = calculate90degreeOrientation(orientation);
		
		// Now we calculate the bounds, and this determines how many 'walls' we have to fit to, and how we proceed
		// We consider only the XY plane, and do a simple Z fit at the end
		
		BidirectionalBound bib = boundCalculator.calcBound(pnt, orientation.createRotationMatrix(), errorNode.add("boundCalculator"));
		
		List<Point3i> pnts = new ArrayList<Point3i>();
		
		// If we have no bounds, we exit
		if (bib.isUnboundedAtBothEnds()) {
			errorNode.add("Orientation is missing bounds at both ends");
			return null;
		}
		
		// we calculate a range from the bounding, say the max
		double range = bib.getMaxOfMax();
		
		BidirectionalBound bib90 = boundCalculator.calcBound(pnt, orientation90.createRotationMatrix(), errorNode.add("boundCalculator90"));
		
		boolean bib90Unbounded = bib90.isUnboundedOrOutsideRange(range); 
		//boolean bib90WithinRatio = bib.ratioBounds(dim) < boundsRatio;
		
		
		//boolean useUnboundedProposerForForward = bib90Unbounded || bib90WithinRatio;
		boolean useUnboundedProposerForForward = bib90Unbounded;
		PointsFromOrientationProposer proposerForward = useUnboundedProposerForForward ? pointsFromOrientationXYUnboundedProposer : pointsFromOrientationXYProposer;
		
		try {
			if (bib.isUnbounded()) {
				
				if (bib90Unbounded) {
					// We calculate points using the 'unbounded' or the bounded finder
					
					Orientation orientationUnbounded = bib.hasForwardDirectionSmallestMax() ? orientation : orientation.negative();
					
					List<List<Point3i>> listOfLists = proposerForward.calcPoints( pnt, orientationUnbounded, dim.getZ()>1, re, true );
					for( List<Point3i> list : listOfLists) {
						pnts.addAll(list);
					}
					
				} else {
					// We use the normal proposer, as we have 2 other opportunities
					List<List<Point3i>> listOfLists = proposerForward.calcPoints( pnt, orientation, dim.getZ()>1, re, false );
					for( List<Point3i> list : listOfLists) {
						pnts.addAll(list);
					}
				}
				
			} else {
				List<List<Point3i>> listOfLists = proposerForward.calcPoints( pnt, orientation, dim.getZ()>1, re, false );
				for( List<Point3i> list : listOfLists) {
					pnts.addAll(list);
				}				
			}
		
		} catch (TraverseOutlineException e) {
			errorNode.add(e);
			return null;
		}
				
		if (pnts.size()==0) {
			errorNode.add("No points found in proposed orientation");
			return null;
		}
		
		// We go in the an angle + 90 degree
		// If we have any problems here, we ignore them rather than terminating
		try {
			if (bib90.isUnboundedOrOutsideRangeAtBothEnds(range)) {
				// We don't add anything else
			} else if (bib90Unbounded) {
				Orientation orientationUnbounded = bib.hasForwardDirectionSmallestMax() ? orientation90 : orientation90.negative();
				
				List<List<Point3i>> listOfLists = pointsFromOrientationXYUnboundedProposerSecondUse.calcPoints( new Point3d(pntRoot), orientationUnbounded, dim.getZ()>1, re, true );
				for( List<Point3i> list : listOfLists) {
					pnts.addAll( list );
				}
				
			} else {
				
				List<List<Point3i>> listOfLists = pointsFromOrientationXYProposerSecondUse.calcPoints( new Point3d(pntRoot), orientation90, dim.getZ()>1, re, false );
				//if (bib90WithinRatio) {
				
				for( List<Point3i> list : listOfLists) {
					pnts.addAll( list );
				}
				
				//} else {
					// We give up
					//pnts.addAll( pointsFromOrientationXYUnboundedProposerSecondUse.calcPoints( new Point3d(pntRoot), orientation90, dim.getZ()>1, range, re, true ) );
				//}
				
				
			}
		} catch (TraverseOutlineException e) {
			errorNode.add(e);
		}		
		
		
		// We go in the Z direction
//		try {
//			//RotationMatrix rotMat = new RotationMatrix(3);
//			//rotMat.s
//			
//			Orientation3DEulerAngles orientationUp = new Orientation3DEulerAngles(0.0 , Math.PI/2, 0.0 );
//			pnts.addAll( pointsFromOrientationZProposer.calcPoints( new Point3d(pntRoot), orientationUp, dim.getZ()>1, -1 ) );
//			
//		} catch (TraverseOutlineException e) {
//			errorNode.add(e);
//			return null;
//		}
		
		return pnts;
	}

	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return orientationXYProposer.isCompatibleWith(testMark);
	}

	@Override
	public ICreateProposalVisualization proposalVisualization(boolean detailed) {
		CreateProposeVisualizationList list = new CreateProposeVisualizationList();
		list.add( pointsFromOrientationXYProposer.proposalVisualization(detailed) );
		list.add( pointsFromOrientationXYUnboundedProposer.proposalVisualization(detailed) );
		
		list.add( pointsFromOrientationXYProposerSecondUse.proposalVisualization(detailed) );
		list.add( pointsFromOrientationXYUnboundedProposerSecondUse.proposalVisualization(detailed) );
		return list;
	}

	public OrientationProposer getOrientationXYProposer() {
		return orientationXYProposer;
	}

	public void setOrientationXYProposer(OrientationProposer orientationXYProposer) {
		this.orientationXYProposer = orientationXYProposer;
	}

	public PointsFromOrientationProposer getPointsFromOrientationXYProposer() {
		return pointsFromOrientationXYProposer;
	}

	public void setPointsFromOrientationXYProposer(
			PointsFromOrientationProposer pointsFromOrientationXYProposer) {
		this.pointsFromOrientationXYProposer = pointsFromOrientationXYProposer;
	}

	public BoundCalculator getBoundCalculator() {
		return boundCalculator;
	}

	public void setBoundCalculator(BoundCalculator boundCalculator) {
		this.boundCalculator = boundCalculator;
	}

	public PointsFromOrientationProposer getPointsFromOrientationXYUnboundedProposer() {
		return pointsFromOrientationXYUnboundedProposer;
	}

	public void setPointsFromOrientationXYUnboundedProposer(
			PointsFromOrientationProposer pointsFromOrientationXYUnboundedProposer) {
		this.pointsFromOrientationXYUnboundedProposer = pointsFromOrientationXYUnboundedProposer;
	}

	public double getBoundsRatio() {
		return boundsRatio;
	}

	public void setBoundsRatio(double boundsRatio) {
		this.boundsRatio = boundsRatio;
	}
}
