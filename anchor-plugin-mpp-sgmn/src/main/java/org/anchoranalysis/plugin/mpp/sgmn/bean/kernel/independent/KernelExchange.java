package org.anchoranalysis.plugin.mpp.sgmn.bean.kernel.independent;

import org.anchoranalysis.anchor.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.anchor.mpp.mark.Mark;
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
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.mpp.sgmn.bean.kernel.KernelIndependent;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcContext;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcNRGException;

import anchor.provider.bean.ProposalAbnormalFailureException;
import ch.ethz.biol.cell.mpp.mark.pxlmark.memo.PxlMarkMemo;
import ch.ethz.biol.cell.mpp.nrg.CfgNRGPixelized;
import ch.ethz.biol.cell.mpp.pair.ListUpdatableMarkSetCollection;
import ch.ethz.biol.cell.mpp.pair.UpdateMarkSetException;

public class KernelExchange extends KernelIndependent<CfgNRGPixelized> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 175391639916706075L;

	// START BEAN PROPERTIES
	@BeanField
	private MarkProposer markProposer = null;
	// END BEAN PROPERTIES
		
	private Mark markExst;
	private Mark markNew;
	
	public KernelExchange() {
	}
	
	@Override
	public double calcAccptProb( int exstSize, int propSize, double poisson_intens, ImageDim scene_size, double densityRatio ) {
		return Math.min(1.0, densityRatio );
	}

	@Override
	public String dscrLast() {
		if (markNew != null) {
			return String.format("%s(%d)", getBeanName(), markExst.getId() );
		} else {
			return getBeanName();
		}
	}

	@Override
	public void updateAfterAccpt(ListUpdatableMarkSetCollection updatableMarkSetCollection,
			CfgNRGPixelized exst, CfgNRGPixelized accptd) throws UpdateMarkSetException {
		
		PxlMarkMemo memoNew = accptd.getMemoForMark( markNew );
		exst.exchangeOnUpdatablePairList( updatableMarkSetCollection, markExst, memoNew  );
	}
	
	@Override
	public int[] changedMarkIDArray() {
		return new int[]{ markExst.getId(), markNew.getId() };
	}
	
	@Override
	public CfgNRGPixelized makeProposal(
		CfgNRGPixelized exst,
		KernelCalcContext context) throws KernelCalcNRGException {

		ProposerContext propContext = context.proposer(); 
		
		// Pick an element from the existing configuration
		int index = exst.getCfg().randomIndex( propContext.getRe() );
		
		markExst = exst.getCfg().get(index);
		
		// We copy the particular mark in question
		markNew = markExst.duplicate();
						
		PxlMarkMemo pmmMarkNew = propContext.create(markNew );
		
		// Change the location of the mark
		boolean succ;
		try {
			succ = markProposer.propose( pmmMarkNew, propContext );
		} catch (ProposalAbnormalFailureException e) {
			throw new KernelCalcNRGException(
				"Failed to propose a mark due to abnormal exception",
				e
			);
		}
		
		if (!succ) {
			return null;
		}

		// We calculate a new NRG by exchanging our marks
		CfgNRGPixelized newNRG = exst.shallowCopy();
		try {
			newNRG.exchange(index, pmmMarkNew, propContext.getNrgStack() );
		} catch (FeatureCalcException e) {
			throw new KernelCalcNRGException( 
				String.format("Cannot exchange index %d",index),
				e
			);
		}
		
		return newNRG;
	}

	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return markProposer.isCompatibleWith(testMark);
	}

	public MarkProposer getMarkProposer() {
		return markProposer;
	}

	public void setMarkProposer(MarkProposer markProposer) {
		this.markProposer = markProposer;
	}
}
