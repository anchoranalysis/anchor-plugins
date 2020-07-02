package org.anchoranalysis.plugin.mpp.bean.proposer.mark;

import java.util.Optional;

import org.anchoranalysis.anchor.mpp.bean.bound.BoundCalculator;
import org.anchoranalysis.anchor.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.anchor.mpp.bound.BidirectionalBound;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipse;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.anchor.mpp.proposer.visualization.CreateProposalVisualization;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.PxlMarkMemo;

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
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.image.orientation.Orientation2D;

public class PositionWalk extends MarkProposer {

	// START BEAN
	@BeanField
	private double boundsRatio = 1.7;
	
	@BeanField
	private BoundCalculator boundCalculator;
	// END BEAN

	@Override
	public boolean propose(PxlMarkMemo inputMark, ProposerContext context) throws ProposalAbnormalFailureException {

		MarkEllipse inputMarkCast = (MarkEllipse) inputMark.getMark();
		
		// We keep picking a random angle, checking the bounds, and nudging 
		// the position in that direction 10 times
		
		Point3d pnt = inputMarkCast.getPos();
		
		int numIter = 10;
		for (int i=0; i<numIter; i++) {
			
			double angle = context.getRandomNumberGenerator().nextDouble() * Math.PI * 2;
			BidirectionalBound bib;
			try {
				bib = boundCalculator.calcBound(
					pnt,
					new Orientation2D(angle).createRotationMatrix()
				);
			} catch (OperationFailedException e) {
				throw new ProposalAbnormalFailureException(e);
			}
			
			if (bib==null || bib.isUnbounded()) {
				continue;
			}
			
			double rb = bib.ratioBounds(context.getDimensions()); 
			if (rb > boundsRatio) {
				continue;
			}
			
			if (bib.getReverse().getMax() > bib.getForward().getMax()) {
				angle = angle + Math.PI;
			}
			
			// Now we walk a unit pixel towards the max in this orientation
			//Orientation2D orientation = new Orientation2D(angle);
			//DoubleMatrix2D mat = orientation.createRotationMatrix();
			
			
			double xNew = pnt.getX() + Math.cos(angle);
			double yNew = pnt.getY() + Math.sin(angle);
			
			Point3d pntNew = new Point3d(xNew,yNew,pnt.getZ());
			if (!context.getDimensions().contains(pntNew)) {
				continue;
			}
			
			pnt = pntNew;
		}
		
		assert( context.getDimensions().contains(pnt) );
		inputMarkCast.setMarksExplicit(pnt, inputMarkCast.getOrientation(), inputMarkCast.getRadii());
		inputMark.reset();
		
		return true;
	}
	
	@Override
	public Optional<CreateProposalVisualization> proposalVisualization(boolean detailed) {
		return Optional.empty();
	}
	
	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return testMark instanceof MarkEllipse;
	}
	
	public BoundCalculator getBoundCalculator() {
		return boundCalculator;
	}

	public void setBoundCalculator(BoundCalculator boundCalculator) {
		this.boundCalculator = boundCalculator;
	}
}
