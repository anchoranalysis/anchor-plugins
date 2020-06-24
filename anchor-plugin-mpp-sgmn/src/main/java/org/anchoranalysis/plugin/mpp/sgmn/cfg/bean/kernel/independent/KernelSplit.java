package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.kernel.independent;

import java.util.Optional;

import org.anchoranalysis.anchor.mpp.bean.proposer.MarkFromCfgProposer;
import org.anchoranalysis.anchor.mpp.bean.proposer.MarkSplitProposer;
import org.anchoranalysis.anchor.mpp.feature.mark.ListUpdatableMarkSetCollection;
import org.anchoranalysis.anchor.mpp.feature.mark.MemoList;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.set.UpdateMarkSetException;
import org.anchoranalysis.anchor.mpp.pair.PairPxlMarkMemo;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
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
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.nrg.NRGStack;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.mpp.sgmn.bean.kernel.KernelPosNeg;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcContext;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcNRGException;

public class KernelSplit extends KernelPosNeg<CfgNRGPixelized> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2994479857672199038L;
	/**
	 * 
	 */

	// START BEAN
	@BeanField
	private MarkSplitProposer splitProposer = null;
	
	@BeanField
	private MarkFromCfgProposer markFromCfgProposer = null;
	// END BEAN
	
	private transient int markExstIndex;
	private transient Optional<Mark> markExst;
	private transient PairPxlMarkMemo pairNew;

	@Override
	public Optional<CfgNRGPixelized> makeProposal(Optional<CfgNRGPixelized> exst, KernelCalcContext context ) throws KernelCalcNRGException {
		
		if (exst.isPresent()) {
			return Optional.empty();
		}
		
		ProposerContext propContext = context.proposer();
		
		try {
			this.markExst = markFromCfgProposer.markFromCfg( exst.get().getCfg(),	propContext	);
		} catch (ProposalAbnormalFailureException e) {
			throw new KernelCalcNRGException(
				"Could not propose a mark due to an abnormal exception",
				e
			);
		}
		
		if (!markExst.isPresent()) {
			propContext.getErrorNode().add("cannot find an existing mark to split");
			pairNew = null;
			markExst = null;
			return Optional.empty();
		}
		
		this.markExstIndex = exst.get().getCfg().indexOf( markExst.get() );
		PxlMarkMemo pmmMarkExst = exst.get().getMemoForIndex(markExstIndex);
		
		try {
			// Let's get positions for our two marks
			pairNew = splitProposer.propose(
				pmmMarkExst,
				propContext,
				context.cfgGen().getCfgGen()
			);
		} catch (ProposalAbnormalFailureException e) {
			throw new KernelCalcNRGException(
				"Failed to propose a mark-split due to abnormal exception",
				e
			);
		}
		
		if (pairNew==null) {
			return Optional.empty();
		}
		
		return Optional.of(
			createCfgNRG( exst.get(), propContext.getNrgStack().getNrgStack() )
		);
	}

	
	
	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return splitProposer.isCompatibleWith(testMark) && markFromCfgProposer.isCompatibleWith(testMark);
	}
	
	
	
	protected CfgNRGPixelized createCfgNRG(CfgNRGPixelized exst, NRGStack nrgStack ) throws KernelCalcNRGException {
		
		// We calculate a new NRG by exchanging our marks
		CfgNRGPixelized newNRG = exst.shallowCopy();
		try {
			newNRG.rmv( markExstIndex, nrgStack );
		} catch (FeatureCalcException e1) {
			throw new KernelCalcNRGException(
				String.format("Cannot remove index %d", markExstIndex ),
				e1
			);
		}
		
		try {
			newNRG.add( pairNew.getSource(), nrgStack );
			newNRG.add( pairNew.getDestination(), nrgStack );
		} catch (FeatureCalcException e) {
			throw new KernelCalcNRGException(
				"Cannot add source and destination",
				e
			);
		}

		return newNRG;
	}
	
	
	@Override
	public double calcAccptProb(int exstSize, int propSize,
			double poisson_intens, ImageDim scene_size, double densityRatio) {
		return densityRatio;
	}

	@Override
	public String dscrLast() {
		if (markExst.isPresent() && pairNew !=null && pairNew.getSource()!=null && pairNew.getDestination()!=null) {
			return String.format(
				"%s %d into %d into %d", getBeanName(),
				markExst.get().getId(),
				pairNew.getSource().getMark().getId(),
				pairNew.getDestination().getMark().getId()
			);
		} else {
			return getBeanName();
		}
	}


	@Override
	public void updateAfterAccpt(ListUpdatableMarkSetCollection updatableMarkSetCollection,
			CfgNRGPixelized exst, CfgNRGPixelized accptd) throws UpdateMarkSetException {
		
		MemoList memoList = exst.createDuplicatePxlMarkMemoList();
		
		PxlMarkMemo memoExst = exst.getMemoForMark( this.markExst.get() );
		
		updatableMarkSetCollection.rmv( memoList, memoExst );
		memoList.remove( memoExst );
		
		PxlMarkMemo memoAdded1 = pairNew.getSource();
		
		// Should always find one
		assert memoAdded1!=null;
		updatableMarkSetCollection.add( memoList, memoAdded1 );
		
		memoList.add(memoAdded1);
		
		PxlMarkMemo memoAdded2 = pairNew.getDestination();
		assert memoAdded2!=null;
		
		updatableMarkSetCollection.add(memoList, memoAdded2 );
	}

	@Override
	public int[] changedMarkIDArray() {
		return new int[]{
			markExst.get().getId(),
			pairNew.getSource().getMark().getId(),
			pairNew.getDestination().getMark().getId()
		};
	}

	public MarkSplitProposer getSplitProposer() {
		return splitProposer;
	}


	public void setSplitProposer(MarkSplitProposer splitProposer) {
		this.splitProposer = splitProposer;
	}


	public MarkFromCfgProposer getMarkFromCfgProposer() {
		return markFromCfgProposer;
	}


	public void setMarkFromCfgProposer(MarkFromCfgProposer markFromCfgProposer) {
		this.markFromCfgProposer = markFromCfgProposer;
	}
}
