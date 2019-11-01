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
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.proposer.error.ErrorNode;
import org.anchoranalysis.anchor.mpp.proposer.visualization.ICreateProposalVisualization;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.orientation.Orientation;

import ch.ethz.biol.cell.mpp.mark.ellipsoidfitter.outlinepixelsretriever.TraverseOutlineException;
import ch.ethz.biol.cell.mpp.mark.proposer.pointsfromorientation.PointsFromOrientationProposer;

public class ContourPointsFromOrientationPointsProposer extends PointsProposer {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private OrientationProposer orientationProposer;
	
	@BeanField
	private PointsFromOrientationProposer pointsFromOrientationProposer;
	
	@BeanField
	private boolean forwardOrientationOnly = false;
	// END BEAN PROPERTIES
	
	@Override
	public List<Point3i> propose(Point3d pnt, Mark mark, ImageDim dim, RandomNumberGenerator re, ErrorNode errorNode) {
		
		Orientation orientation = orientationProposer.propose(mark, dim, re, errorNode.add("orientationProposer") );
		
		if (orientation==null) {
			return null;
		}
	
		try {
			List<List<Point3i>> listOfLists = pointsFromOrientationProposer.calcPoints( pnt, orientation, dim.getZ()>1, re, forwardOrientationOnly );
			
			List<Point3i> listOut = new ArrayList<Point3i>();
			for( List<Point3i> list : listOfLists ) {
				listOut.addAll(list);
			}
			return listOut;
			
		} catch (TraverseOutlineException e) {
			errorNode.add(e);
			return null;
		}
	}

	public OrientationProposer getOrientationProposer() {
		return orientationProposer;
	}

	public void setOrientationProposer(OrientationProposer orientationProposer) {
		this.orientationProposer = orientationProposer;
	}



	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return orientationProposer.isCompatibleWith(testMark);
	}

	@Override
	public ICreateProposalVisualization proposalVisualization(boolean detailed) {
		return pointsFromOrientationProposer.proposalVisualization(detailed);
	}

	public PointsFromOrientationProposer getPointsFromOrientationProposer() {
		return pointsFromOrientationProposer;
	}

	public void setPointsFromOrientationProposer(
			PointsFromOrientationProposer pointsFromOrientationProposer) {
		this.pointsFromOrientationProposer = pointsFromOrientationProposer;
	}

	public boolean isForwardOrientationOnly() {
		return forwardOrientationOnly;
	}

	public void setForwardOrientationOnly(boolean forwardOrientationOnly) {
		this.forwardOrientationOnly = forwardOrientationOnly;
	}

}
