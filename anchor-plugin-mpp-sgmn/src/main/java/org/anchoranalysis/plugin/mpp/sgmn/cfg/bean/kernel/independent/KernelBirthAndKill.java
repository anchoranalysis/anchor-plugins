package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.kernel.independent;

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


import java.util.List;

import org.anchoranalysis.anchor.mpp.bean.cfg.CfgGen;
import org.anchoranalysis.anchor.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.anchor.mpp.feature.mark.ListUpdatableMarkSetCollection;
import org.anchoranalysis.anchor.mpp.feature.mark.PxlMarkMemoList;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.anchor.mpp.mark.GlobalRegionIdentifiers;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.set.UpdateMarkSetException;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.PxlMarkMemo;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.mpp.sgmn.bean.kernel.KernelPosNeg;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcContext;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcNRGException;

public class KernelBirthAndKill extends KernelPosNeg<CfgNRGPixelized> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5973179354519885659L;

	// START BEANS
	@BeanField
	private double overlapRatioThreshold = 0.1;

	@BeanField
	private MarkProposer markProposer = null;
	
	// Optional proposal for doing an additional birth
	@BeanField @OptionalBean
	private MarkProposer markProposerAdditionalBirth = null;
	
	@BeanField
	private int regionID = GlobalRegionIdentifiers.SUBMARK_INSIDE;
	// END BEANS

	
	
	private Mark markNew;
	private Mark markNewAdditional;
	
	private transient List<PxlMarkMemo> toKill;
	
	public KernelBirthAndKill() {
	}
	
	@Override
	public CfgNRGPixelized makeProposal(CfgNRGPixelized exst, KernelCalcContext context) throws KernelCalcNRGException {

		markNew = context.cfgGen().getCfgGen().newTemplateMark();
		
		ProposerContext propContext = context.proposer();
		
		PxlMarkMemo memoNew = propContext.create( markNew );
		if (!proposeMark(memoNew, propContext)) {
			return null;
		}
		
		try {	
			toKill = KernelBirthAndKillHelper.determineKillObjects(
				memoNew,
				exst,
				regionID,
				overlapRatioThreshold
			);
		} catch ( OperationFailedException e ) {
			throw new KernelCalcNRGException("Cannot add kill-objects", e );
		}
			

		// Now we want to do another birth, and we take this somewhere from the memos, 
		//  of the killed objects that isn't covered by the birthMark
		PxlMarkMemo pmmAdditional = maybeMakeAdditionalBirth(
			memoNew.getMark(),
			context.cfgGen().getCfgGen(),
			context
		);
				
		return KernelBirthAndKillHelper.calcUpdatedNRG(exst, memoNew, pmmAdditional, toKill, context, propContext);
	}
	
	private PxlMarkMemo maybeMakeAdditionalBirth( Mark markNew, CfgGen cfgGen, KernelCalcContext context ) throws KernelCalcNRGException {
		if (markProposerAdditionalBirth!=null) {
			PxlMarkMemo pmmAdditional = KernelBirthAndKillHelper.makeAdditionalBirth(
				markProposerAdditionalBirth,
				markNew,
				toKill,
				cfgGen,
				regionID,
				context
			);
			if (pmmAdditional!=null) {
				markNewAdditional = pmmAdditional.getMark();
			}
			return pmmAdditional;
		} else {
			return null;
		}
	}
		
	private boolean proposeMark( PxlMarkMemo memoNew, ProposerContext context )
			throws KernelCalcNRGException {
		try {
			if (!markProposer.propose(memoNew, context)) {
				return false;
			}
			return true;
		} catch (ProposalAbnormalFailureException e) {
			throw new KernelCalcNRGException(
				"Failed to propose a mark due to abnormal exception%n%s%n",
				e
			);
		}	
	}
	
	@Override
	public double calcAccptProb(int exstSize, int propSize,
			double poisson_intens, ImageDim scene_size, double densityRatio) {
		
        double num = getProbNeg() * scene_size.getVolume() * poisson_intens;
        double dem = getProbPos() * propSize;
        
        assert num>0;
        assert dem>0;
		
        double d = (densityRatio * num) / dem;
        
        assert !Double.isNaN(d);
        
        return Math.min( d, 1.0);
	}
	
	
	@Override
	public String dscrLast() {
		return String.format("birthAndKill(%d)", markNew.getId() );
	}

	@Override
	public void updateAfterAccpt(
			ListUpdatableMarkSetCollection updatableMarkSetCollection, CfgNRGPixelized exst, CfgNRGPixelized accptd) throws UpdateMarkSetException {
		
		PxlMarkMemoList memoList = exst.createDuplicatePxlMarkMemoList(); 
		
		addNewMark( this.markNew, updatableMarkSetCollection, exst, accptd, memoList );
		
		removeMarks(toKill, updatableMarkSetCollection, memoList);
	
		if (markNewAdditional!=null) {
			addAdditionalMark(markNewAdditional, updatableMarkSetCollection, memoList, accptd);
		}
	}
	
	private static void addNewMark( Mark mark, ListUpdatableMarkSetCollection updatableMarkSetCollection, CfgNRGPixelized exst, CfgNRGPixelized accptd, PxlMarkMemoList memoList ) throws UpdateMarkSetException {
		PxlMarkMemo memo = accptd.getMemoForMark( mark );
		
		exst.addToUpdatablePairList( updatableMarkSetCollection, memo );
		memoList.add(memo);		
	}
		
	private static void removeMarks( List<PxlMarkMemo> marks, ListUpdatableMarkSetCollection updatableMarkSetCollection, PxlMarkMemoList memoList ) throws UpdateMarkSetException {
		for( PxlMarkMemo memoRmv : marks) {
			updatableMarkSetCollection.rmv(memoList, memoRmv);
			memoList.remove(memoRmv);
			// TODO come up with a better system other than these memoLists
		}
		
	}
	
	private static void addAdditionalMark(Mark markAdditional, ListUpdatableMarkSetCollection updatableMarkSetCollection, PxlMarkMemoList memoList, CfgNRGPixelized accptd) throws UpdateMarkSetException {
		PxlMarkMemo memoNewAdditional = accptd.getMemoForMark( markAdditional );
		updatableMarkSetCollection.add(memoList, memoNewAdditional );
		memoList.add(memoNewAdditional);		
	}
	
	@Override
	public int[] changedMarkIDArray() {
		return new int[]{ this.markNew.getId() };
	}

	public MarkProposer getMarkProposer() {
		return markProposer;
	}

	public void setMarkProposer(MarkProposer markProposer) {
		this.markProposer = markProposer;
	}

	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return markProposer.isCompatibleWith(testMark);
	}

	public double getOverlapRatioThreshold() {
		return overlapRatioThreshold;
	}

	public void setOverlapRatioThreshold(double overlapRatioThreshold) {
		this.overlapRatioThreshold = overlapRatioThreshold;
	}

	public MarkProposer getMarkProposerAdditionalBirth() {
		return markProposerAdditionalBirth;
	}

	public void setMarkProposerAdditionalBirth(
			MarkProposer markProposerAdditionalBirth) {
		this.markProposerAdditionalBirth = markProposerAdditionalBirth;
	}

	public int getRegionID() {
		return regionID;
	}

	public void setRegionID(int regionID) {
		this.regionID = regionID;
	}
}
