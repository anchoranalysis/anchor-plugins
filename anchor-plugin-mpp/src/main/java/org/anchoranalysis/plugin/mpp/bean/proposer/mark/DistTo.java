package org.anchoranalysis.plugin.mpp.bean.proposer.mark;

import org.anchoranalysis.anchor.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.MarkDistance;
import org.anchoranalysis.anchor.mpp.mark.UnsupportedMarkTypeException;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.anchor.mpp.proposer.error.ErrorNode;
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

public class DistTo extends MarkProposerOne {

	// BEAN PARAMETERS
	@BeanField
	private double maxDist;
	
	@BeanField
	private double minDist;
	
	@BeanField
	private int maxNumTries = -1;

	@BeanField
	private MarkDistance distance;
	// END BEAN PARAMETERS

	@Override
	protected boolean propose(PxlMarkMemo inputMark, ProposerContext context, MarkProposer source)
			throws ProposalAbnormalFailureException {
	
		ErrorNode errorNode = context.getErrorNode().add("MarkProposerDistTo");
		
		Mark markOld = inputMark.getMark().duplicate();
		
		double d;
		
		int tryNum = 0;
		do {
			
			boolean succ = source.propose(inputMark, context);
			if (!succ) {
				return false;
			}
						
			try {
				d = distance.distance(markOld, inputMark.getMark());
			} catch (UnsupportedMarkTypeException e) {
				errorNode.add(e.toString());
				return false;
			}
			
			if (tryNum++==maxNumTries) {
				if (errorNode!=null) {
					errorNode.add("maxNumTries reached");
	        	}
				return false;
			}
		} while( d < minDist || d > maxDist );

		return true;
	}

	public double getMaxDist() {
		return maxDist;
	}

	public void setMaxDist(double maxDist) {
		this.maxDist = maxDist;
	}

	public double getMinDist() {
		return minDist;
	}

	public void setMinDist(double minDist) {
		this.minDist = minDist;
	}

	public int getMaxNumTries() {
		return maxNumTries;
	}

	public void setMaxNumTries(int maxNumTries) {
		this.maxNumTries = maxNumTries;
	}

	public MarkDistance getDistance() {
		return distance;
	}

	public void setDistance(MarkDistance distance) {
		this.distance = distance;
	}
}
