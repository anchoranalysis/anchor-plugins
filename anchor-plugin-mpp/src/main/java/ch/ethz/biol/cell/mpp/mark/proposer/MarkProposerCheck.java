package ch.ethz.biol.cell.mpp.mark.proposer;

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


import java.awt.Color;

import org.anchoranalysis.anchor.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.anchor.mpp.proposer.visualization.ICreateProposalVisualization;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.PxlMarkMemo;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.color.RGBColor;
import org.anchoranalysis.core.error.OperationFailedException;

import ch.ethz.biol.cell.core.CheckMark;
import ch.ethz.biol.cell.mpp.gui.videostats.internalframe.markredraw.ColoredCfg;
import ch.ethz.biol.cell.mpp.mark.check.CheckException;

public class MarkProposerCheck extends MarkProposer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -905238995760827400L;
	
	// BEAN PARAMETERS
	@BeanField
	private MarkProposer markProposer = null;
	
	@BeanField
	private CheckMark checkMark = null;
	// END BEAN

	private Mark lastFailedMark;
	
	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return markProposer.isCompatibleWith(testMark) && checkMark.isCompatibleWith(testMark);
	}

	@Override
	public boolean propose(PxlMarkMemo inputMark, ProposerContext context) throws ProposalAbnormalFailureException {
		
		if (!markProposer.propose(inputMark, context)) {
			lastFailedMark = null;
			return false;
		}
		
		try {
			checkMark.start( context.getNrgStack() );
		} catch (OperationFailedException e) {
			context.getErrorNode().add(e);
			return false;
		}
		
		try {
			if (!checkMark.check(
				inputMark.getMark(),
				context.getRegionMap(),
				context.getNrgStack()
			)) {
				lastFailedMark = inputMark.getMark();
				return false;
			}
		} catch (CheckException e) {
			checkMark.end();
			throw new ProposalAbnormalFailureException("Failed to check-mark due to abnormal exception", e);
		}
		
		lastFailedMark = null;
		
		checkMark.end();
		
		return true;
	}

	public MarkProposer getMarkProposer() {
		return markProposer;
	}



	public void setMarkProposer(MarkProposer markProposer) {
		this.markProposer = markProposer;
	}



	public CheckMark getCheckMark() {
		return checkMark;
	}



	public void setCheckMark(CheckMark checkMark) {
		this.checkMark = checkMark;
	}



	public ICreateProposalVisualization proposalVisualization(boolean detailed) {
		if (lastFailedMark!=null) {
			return new ICreateProposalVisualization() {
				
				@Override
				public void addToCfg(ColoredCfg cfg) {
					cfg.addChangeID(lastFailedMark, new RGBColor(Color.ORANGE));
				}
			};
		} else {
			return markProposer.proposalVisualization(detailed);
		}
	}
}
