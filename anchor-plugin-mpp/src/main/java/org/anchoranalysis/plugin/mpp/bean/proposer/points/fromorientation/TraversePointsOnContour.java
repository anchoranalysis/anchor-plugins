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
import java.util.Optional;

import org.anchoranalysis.anchor.mpp.proposer.visualization.CreateProposalVisualization;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.orientation.Orientation;
import org.anchoranalysis.plugin.mpp.bean.proposer.points.onoutline.FindPointOnOutline;
import static org.anchoranalysis.plugin.mpp.bean.proposer.points.fromorientation.VisualizationUtilities.*;

import ch.ethz.biol.cell.mpp.mark.ellipsoidfitter.outlinepixelsretriever.OutlinePixelsRetriever;
import ch.ethz.biol.cell.mpp.mark.ellipsoidfitter.outlinepixelsretriever.TraverseOutlineException;
import lombok.Getter;
import lombok.Setter;

public class TraversePointsOnContour extends PointsFromOrientationProposer {

	// START BEAN PROPERTIES
	@BeanField @Getter @Setter
	private FindPointOnOutline findOutlinePixelAngle;
	
	@BeanField @Getter @Setter
	private OutlinePixelsRetriever outlinePixelsRetriever;
	
	@BeanField @OptionalBean @Getter @Setter
	private OutlinePixelsRetriever outlinePixelsRetrieverReverse;
	// END BEAN PROPERTIES
	
	private boolean do3D;
	private List<Point3i> lastPointsForward = new ArrayList<>();
	private List<Point3i> lastPointsReverse = new ArrayList<>();
	private Optional<Point3i> forwardCenterPoint;
	private Optional<Point3i> reverseCenterPoint;
	
	// Calculates the points in both directions
	@Override
	public List<List<Point3i>> calcPoints( Point3d centerPoint, Orientation orientation, boolean do3D, RandomNumberGenerator re, boolean forwardDirectionOnly ) throws TraverseOutlineException {
		
		this.do3D = do3D;
		
		lastPointsForward.clear();
		lastPointsReverse.clear();
		
		forwardCenterPoint = addPointsFromOrientation( centerPoint, orientation, findOutlinePixelAngle, outlinePixelsRetriever, lastPointsForward, re);
	
		if (!forwardDirectionOnly) {
			OutlinePixelsRetriever reverseRetriever = outlinePixelsRetrieverReverse!=null ? outlinePixelsRetrieverReverse : outlinePixelsRetriever;
			reverseCenterPoint = addPointsFromOrientation( centerPoint, orientation.negative(), findOutlinePixelAngle, reverseRetriever, lastPointsReverse, re);
			
			if (lastPointsForward.isEmpty() && lastPointsReverse.isEmpty()) {
				throw new TraverseOutlineException("Cannot find forward or reverse point to traverse");
			}
		}
		
		if (lastPointsForward.isEmpty() && lastPointsReverse.isEmpty()) {
			throw new TraverseOutlineException("Cannot find outline point");
		}
		
		List<List<Point3i>> combinedLists = new ArrayList<>();
		combinedLists.add(lastPointsForward);
		if (!forwardDirectionOnly) {
			combinedLists.add(lastPointsReverse);
		}
		return combinedLists;
	}
	
	private Optional<Point3i> addPointsFromOrientation(
		Point3d centerPoint,
		Orientation orientation,
		FindPointOnOutline find,
		OutlinePixelsRetriever traverseOutline,
		List<Point3i> listOut,
		RandomNumberGenerator re
	) throws TraverseOutlineException {
		
		try {
			Optional<Point3i> foundPoint = find.pointOnOutline( centerPoint, orientation );
			
			if (foundPoint.isPresent()) {
				traverseOutline.traverse(
					foundPoint.get(),
					listOut,
					re
				);
			}
			
			return foundPoint;
			
		} catch (OperationFailedException e) {
			throw new TraverseOutlineException("Unable to add points from orientation", e);
		}
	}

	public CreateProposalVisualization proposalVisualization(boolean detailed) {
		return cfg -> {
			maybeAddPoints(cfg, lastPointsForward, Color.CYAN);
			maybeAddPoints(cfg, lastPointsReverse, Color.YELLOW);
			
			if (detailed) {
				maybeAddConic(cfg, forwardCenterPoint, Color.MAGENTA, do3D);
				maybeAddConic(cfg, reverseCenterPoint, Color.MAGENTA, do3D);
				maybeAddLineSegment(cfg, forwardCenterPoint, reverseCenterPoint, Color.ORANGE);
			}
		};
	}

	@Override
	public void clearVisualizationState() {
		lastPointsForward.clear();
		lastPointsReverse.clear();
		forwardCenterPoint=Optional.empty();
		reverseCenterPoint=Optional.empty();
	}
}
