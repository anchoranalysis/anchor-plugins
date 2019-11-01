package ch.ethz.biol.cell.mpp.mark.proposer.fromcfg;

import org.anchoranalysis.anchor.mpp.bean.proposer.MarkFromCfgProposer;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;

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

import ch.ethz.biol.cell.mpp.cfg.Cfg;

public class MarkFromCfgProposerRepeat extends MarkFromCfgProposer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4813619115351864179L;
	
	
	// START BEAN
	@BeanField
	private MarkFromCfgProposer markFromCfgProposer;
	
	@BeanField
	private int maxIter = 20;
	// END BEAN
	
	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return markFromCfgProposer.isCompatibleWith(testMark);
	}

	@Override
	public Mark markFromCfg(Cfg cfg, ProposerContext context) throws ProposalAbnormalFailureException {
		
		context.addErrorLevel("MarkFromCfgProposerRepeat");
		
		for (int i=0; i<maxIter; i++) {
			Mark m = markFromCfgProposer.markFromCfg(cfg, context);
			if (m!=null) {
				return m;
			}
		}
		
		context.getErrorNode().add("maxIter reached");
		
		return null;
	}

	public MarkFromCfgProposer getMarkFromCfgProposer() {
		return markFromCfgProposer;
	}

	public void setMarkFromCfgProposer(MarkFromCfgProposer markFromCfgProposer) {
		this.markFromCfgProposer = markFromCfgProposer;
	}

	public int getMaxIter() {
		return maxIter;
	}

	public void setMaxIter(int maxIter) {
		this.maxIter = maxIter;
	}

}
