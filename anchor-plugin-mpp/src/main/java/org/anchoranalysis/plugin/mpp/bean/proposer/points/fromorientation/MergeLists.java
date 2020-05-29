package org.anchoranalysis.plugin.mpp.bean.proposer.points.fromorientation;

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

import org.anchoranalysis.anchor.mpp.proposer.visualization.ICreateProposalVisualization;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.orientation.Orientation;

import ch.ethz.biol.cell.mpp.mark.ellipsoidfitter.outlinepixelsretriever.TraverseOutlineException;

public class MergeLists extends PointsFromOrientationProposer {

	// START BEAN PROPERTIES
	@BeanField
	private PointsFromOrientationProposer pointsFromOrientationProposer;
	// END BEAN PROPERTIEs

	@Override
	public ICreateProposalVisualization proposalVisualization(boolean detailed) {
		return pointsFromOrientationProposer.proposalVisualization(detailed);
	}

	@Override
	public void clearVisualizationState() {
		pointsFromOrientationProposer.clearVisualizationState();
	}

	@Override
	public List<List<Point3i>> calcPoints(Point3d centrePoint,
			Orientation orientation, boolean do3D, RandomNumberGenerator re,
			boolean forwardDirectionOnly) throws TraverseOutlineException {
		
		List<List<Point3i>> listOfLists = pointsFromOrientationProposer.calcPoints(centrePoint, orientation, do3D, re, forwardDirectionOnly);
		
		List<Point3i> combinedList = new ArrayList<Point3i>();
		for( List<Point3i> list : listOfLists ) {
			combinedList.addAll(list);
		}
		
		List<List<Point3i>> listOfListsNew = new ArrayList<List<Point3i>>();
		listOfListsNew.add( combinedList );
		return listOfListsNew;
	}

	public PointsFromOrientationProposer getPointsFromOrientationProposer() {
		return pointsFromOrientationProposer;
	}

	public void setPointsFromOrientationProposer(
			PointsFromOrientationProposer pointsFromOrientationProposer) {
		this.pointsFromOrientationProposer = pointsFromOrientationProposer;
	}

}
