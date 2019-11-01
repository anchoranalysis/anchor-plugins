package ch.ethz.biol.cell.mpp.mark.proposer.merge;

import org.anchoranalysis.anchor.mpp.bean.proposer.MarkMergeProposer;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.index.GetOperationFailedException;

import anchor.provider.bean.ProposalAbnormalFailureException;

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


import ch.ethz.biol.cell.beaninitparams.MPPInitParams;
import ch.ethz.biol.cell.mpp.mark.pxlmark.memo.PxlMarkMemo;

public class MarkMergeProposerReference extends MarkMergeProposer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4122761299434774623L;

	// Start BEAN
	@BeanField
	private String id;
	// End BEAN
	
	private MarkMergeProposer delegate = null;
	
	@Override
	public void onInit(MPPInitParams pso) throws InitException {
		super.onInit(pso);
		try {
			delegate = getSharedObjects().getMarkMergeProposerSet().getException(id);
		} catch (GetOperationFailedException e) {
			throw new InitException( String.format("Cannot find referenced markSplitProposer '%s'", id) );
		}
	}
	
	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return delegate.isCompatibleWith(testMark);
	}

	@Override
	public Mark propose(PxlMarkMemo mark1, PxlMarkMemo mark2, ProposerContext context) throws ProposalAbnormalFailureException {
		return delegate.propose(mark1, mark2, context);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
