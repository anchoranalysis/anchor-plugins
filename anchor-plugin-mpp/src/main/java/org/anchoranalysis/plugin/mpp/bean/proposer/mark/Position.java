package org.anchoranalysis.plugin.mpp.bean.proposer.mark;

import org.anchoranalysis.anchor.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.anchor.mpp.bean.proposer.PositionProposerBean;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.MarkAbstractPosition;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.anchor.mpp.proposer.visualization.ICreateProposalVisualization;
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
import org.anchoranalysis.core.geometry.Point3d;

public class Position extends MarkProposer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8166722494036948387L;
	
	// START BEAN PROPERTIES
	@BeanField
	private PositionProposerBean positionProposer;
	// END BEAN PROPERTIES
	
	public Position() {
		
	}
	
	public Position( PositionProposerBean positionProposer ) {
		this.positionProposer = positionProposer;
	}

	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return testMark instanceof MarkAbstractPosition;
	}

	@Override
	public boolean propose(PxlMarkMemo inputMark, ProposerContext context) {
		
		// Assume this is a MarkPos
		MarkAbstractPosition inputMarkPos = (MarkAbstractPosition) inputMark.getMark();
		inputMark.reset();
				
		// We create a new point, so that if it's changed later it doesn't affect the original home
		Point3d pos = positionProposer.propose(
			context.addErrorLevel("MarkPositionProposer")
		);
	
        if(pos==null) {
        	context.getErrorNode().add("positionProposer returned null");
        	return false;
        }
        inputMarkPos.setPos(pos);
		
        return true;
	}

	public PositionProposerBean getPositionProposer() {
		return positionProposer;
	}

	public void setPositionProposer(PositionProposerBean positionProposer) {
		this.positionProposer = positionProposer;
	}

	@Override
	public ICreateProposalVisualization proposalVisualization(boolean detailed) {
		return null;
	}
}