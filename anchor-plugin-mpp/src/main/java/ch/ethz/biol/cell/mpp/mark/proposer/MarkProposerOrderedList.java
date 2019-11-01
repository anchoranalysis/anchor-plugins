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


import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.anchor.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.anchor.mpp.proposer.visualization.ICreateProposalVisualization;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.NonEmpty;

import anchor.provider.bean.ProposalAbnormalFailureException;
import ch.ethz.biol.cell.mpp.mark.pxlmark.memo.PxlMarkMemo;

public class MarkProposerOrderedList extends MarkProposer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5307009712143133777L;
	
	// START BEAN
	@BeanField @NonEmpty
	private List<MarkProposer> markProposerList = new ArrayList<>();
	// END BEAN
	
	public MarkProposerOrderedList() {
		super();
	}
	
	@Override
	public boolean propose(PxlMarkMemo inputMark, ProposerContext context) throws ProposalAbnormalFailureException {
		
		for (MarkProposer markProposer : markProposerList) {
			// Calculate position
	        if(!markProposer.propose(inputMark, context)) {
	        	return false;
	        }
		}
		return true;
	}

	
	@Override
	public boolean isCompatibleWith(Mark testMark) {
		
		for (MarkProposer markProposer : markProposerList) {
			
			if (!markProposer.isCompatibleWith(testMark)) {
				return false;
			}
		}
		
		return true;
	}

	public List<MarkProposer> getMarkProposerList() {
		return markProposerList;
	}

	public void setMarkProposerList(List<MarkProposer> markProposerList) {
		this.markProposerList = markProposerList;
	}


	@Override
	public ICreateProposalVisualization proposalVisualization(boolean detailed) {
		return null;
	}
}
