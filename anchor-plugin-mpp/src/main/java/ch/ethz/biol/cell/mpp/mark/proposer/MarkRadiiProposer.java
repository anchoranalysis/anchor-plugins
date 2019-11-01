package ch.ethz.biol.cell.mpp.mark.proposer;

import org.anchoranalysis.anchor.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.anchor.mpp.bean.proposer.OrientationProposer;
import org.anchoranalysis.anchor.mpp.bean.proposer.radii.RadiiProposer;
import org.anchoranalysis.anchor.mpp.mark.ISetMarksExplicit;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.anchor.mpp.proposer.visualization.ICreateProposalVisualization;

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
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.image.orientation.Orientation;

import ch.ethz.biol.cell.mpp.mark.pxlmark.memo.PxlMarkMemo;

public class MarkRadiiProposer extends MarkProposer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7481314419224313543L;
	
	// BEAN PARAMETERS
	@BeanField
	private RadiiProposer radiiProposer = null;
	
	@BeanField
	private OrientationProposer orientationProposer = null;
	// END BEAN PARAMETERS
	
	public MarkRadiiProposer() {
		
	}
	
	public MarkRadiiProposer(RadiiProposer radiiProposer,
			OrientationProposer orientationProposer) {
		super();
		this.radiiProposer = radiiProposer;
		this.orientationProposer = orientationProposer;
	}

	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return testMark instanceof ISetMarksExplicit;
	}

	@Override
	public boolean propose(PxlMarkMemo inputMark, ProposerContext context) {
		
		ISetMarksExplicit mark = (ISetMarksExplicit) inputMark.getMark();
		
		Orientation orientationNew = orientationProposer.propose(
			inputMark.getMark(),
			context.getDimensions(),
			context.getRe(),
			context.getErrorNode()
		);

		if (orientationNew==null) {
			context.getErrorNode().add("orientationProposer returned null");
			return false;
		}
		// Now we try to randomzieMarks
		
		context = context.addErrorLevel("MarkRadiiProposer");
		
		assert( context.getDimensions().contains( inputMark.getMark().centerPoint() ));
		
		Point3d rad;
		try {
			rad = radiiProposer.propose(
				inputMark.getMark().centerPoint(),
				getSharedObjects().getMarkBounds(),
				context.getRe(),
				context.getDimensions(),
				orientationNew,
				context.getErrorNode()
			);
		} catch (GetOperationFailedException e) {
			context.getErrorNode().add(e);
			return false;
		}
		
		if (rad==null) {
			context.getErrorNode().add("radiiProposer returned null");
			return false;
		}

		mark.setMarksExplicit( inputMark.getMark().centerPoint(), orientationNew, rad);
		inputMark.reset();
		return true;
	}

	public OrientationProposer getOrientationProposer() {
		return orientationProposer;
	}

	public void setOrientationProposer(OrientationProposer orientationProposer) {
		this.orientationProposer = orientationProposer;
	}

	public RadiiProposer getRadiiProposer() {
		return radiiProposer;
	}

	public void setRadiiProposer(RadiiProposer radiiProposer) {
		this.radiiProposer = radiiProposer;
	}

	@Override
	public ICreateProposalVisualization proposalVisualization(boolean detailed) {
		return null;
	}
}
