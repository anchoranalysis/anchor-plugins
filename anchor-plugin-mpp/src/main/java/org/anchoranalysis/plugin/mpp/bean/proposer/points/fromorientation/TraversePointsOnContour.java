package org.anchoranalysis.plugin.mpp.bean.proposer.points.fromorientation;

/*
 * #%L
 * anchor-mpp
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


import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.anchor.mpp.cfg.ColoredCfg;
import org.anchoranalysis.anchor.mpp.mark.MarkLineSegment;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkConicFactory;
import org.anchoranalysis.anchor.mpp.mark.points.MarkPointListFactory;
import org.anchoranalysis.anchor.mpp.proposer.visualization.ICreateProposalVisualization;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.color.RGBColor;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.orientation.Orientation;

import ch.ethz.biol.cell.mpp.mark.ellipsoidfitter.findoutlinepixelangle.FindOutlinePixelAngle;
import ch.ethz.biol.cell.mpp.mark.ellipsoidfitter.outlinepixelsretriever.OutlinePixelsRetriever;
import ch.ethz.biol.cell.mpp.mark.ellipsoidfitter.outlinepixelsretriever.TraverseOutlineException;

public class TraversePointsOnContour extends PointsFromOrientationProposer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6882300188460868470L;
	
	// START BEAN PROPERTIES
	@BeanField
	private FindOutlinePixelAngle findOutlinePixelAngle;
	
	@BeanField
	private OutlinePixelsRetriever outlinePixelsRetriever;
	
	@BeanField @OptionalBean
	private OutlinePixelsRetriever outlinePixelsRetrieverReverse;
	// END BEAN PROPERTIES
	
	private boolean do3D;
	private List<Point3i> lastPntsForward = new ArrayList<>();
	private List<Point3i> lastPntsReverse = new ArrayList<>();
	private Point3d forwardCentrePoint;
	private Point3d reverseCentrePoint;
	
	public TraversePointsOnContour() {
		super();
	}
	
	// Calculates the points in both directions
	@Override
	public List<List<Point3i>> calcPoints( Point3d centrePoint, Orientation orientation, boolean do3D, RandomNumberGenerator re, boolean forwardDirectionOnly ) throws TraverseOutlineException {
		
		this.do3D = do3D;
		
		lastPntsForward.clear();
		lastPntsReverse.clear();
		
		forwardCentrePoint = addPointsFromOrientation( centrePoint, orientation, findOutlinePixelAngle, outlinePixelsRetriever, lastPntsForward, "forward", re);
	
		if (!forwardDirectionOnly) {
			OutlinePixelsRetriever reverseRetriever = outlinePixelsRetrieverReverse!=null ? outlinePixelsRetrieverReverse : outlinePixelsRetriever;
			reverseCentrePoint = addPointsFromOrientation( centrePoint, orientation.negative(), findOutlinePixelAngle, reverseRetriever, lastPntsReverse, "reverse", re);
			
			if (lastPntsForward.size()==0 && lastPntsReverse.size()==0) {
				throw new TraverseOutlineException("Cannot find forward or reverse point to traverse");
			}
		}
		
		if (lastPntsForward.size()==0 && lastPntsReverse.size()==0) {
			throw new TraverseOutlineException("Cannot find outline point");
		}
		
		List<List<Point3i>> combinedLists = new ArrayList<List<Point3i>>();
		combinedLists.add(lastPntsForward);
		if (!forwardDirectionOnly) {
			combinedLists.add(lastPntsReverse);
		}
		return combinedLists;
	}
	
	private Point3d addPointsFromOrientation( Point3d centrePoint, Orientation orientation, FindOutlinePixelAngle find, OutlinePixelsRetriever traverseOutline, List<Point3i> listOut, String desc, RandomNumberGenerator re ) throws TraverseOutlineException {
		
		try {
			Point3d found_point = find.pointOnOutline( centrePoint, orientation );
			
			if (found_point!=null) {
				traverseOutline.traverse(
					new Point3i( (int) found_point.getX(), (int) found_point.getY(), (int) found_point.getZ()),
					listOut,
					re
				);
			}
			return found_point;
			
		} catch (OperationFailedException e) {
			throw new TraverseOutlineException("Unable to add points from orientation", e);
		}
	}

	public FindOutlinePixelAngle getFindOutlinePixelAngle() {
		return findOutlinePixelAngle;
	}

	public void setFindOutlinePixelAngle(FindOutlinePixelAngle findOutlinePixelAngle) {
		this.findOutlinePixelAngle = findOutlinePixelAngle;
	}

	public OutlinePixelsRetriever getOutlinePixelsRetriever() {
		return outlinePixelsRetriever;
	}

	public void setOutlinePixelsRetriever(
			OutlinePixelsRetriever outlinePixelsRetriever) {
		this.outlinePixelsRetriever = outlinePixelsRetriever;
	}

	public ICreateProposalVisualization proposalVisualization(final boolean detailed) {
		return new ICreateProposalVisualization() {
			
			@Override
			public void addToCfg(ColoredCfg cfg) {
				if (lastPntsForward!=null&& lastPntsForward.size()>0) {
					cfg.addChangeID( MarkPointListFactory.createMarkFromPoints3i(lastPntsForward), new RGBColor(Color.CYAN) );
				}
				
				if (lastPntsReverse!=null && lastPntsReverse.size()>0) {
					cfg.addChangeID( MarkPointListFactory.createMarkFromPoints3i(lastPntsReverse), new RGBColor(Color.YELLOW) );
				}
				
				if (detailed) {
					if (forwardCentrePoint!=null) {
						cfg.addChangeID( MarkConicFactory.createMarkFromPoint3d(forwardCentrePoint,1,do3D), new RGBColor(Color.MAGENTA) );
					}
					
					if (reverseCentrePoint!=null) {
						cfg.addChangeID( MarkConicFactory.createMarkFromPoint3d(reverseCentrePoint,1,do3D), new RGBColor(Color.MAGENTA) );
					}
					
					if (forwardCentrePoint!=null && reverseCentrePoint!=null) {
						MarkLineSegment mls = new MarkLineSegment();
						mls.setPoints(forwardCentrePoint, reverseCentrePoint);
						cfg.addChangeID( mls, new RGBColor(Color.ORANGE) );
					}
				}
			}
		};
	}

	@Override
	public void clearVisualizationState() {
		lastPntsForward.clear();
		lastPntsReverse.clear();
		forwardCentrePoint=null;
		reverseCentrePoint=null;
	}

	public OutlinePixelsRetriever getOutlinePixelsRetrieverReverse() {
		return outlinePixelsRetrieverReverse;
	}

	public void setOutlinePixelsRetrieverReverse(
			OutlinePixelsRetriever outlinePixelsRetrieverReverse) {
		this.outlinePixelsRetrieverReverse = outlinePixelsRetrieverReverse;
	}
}
