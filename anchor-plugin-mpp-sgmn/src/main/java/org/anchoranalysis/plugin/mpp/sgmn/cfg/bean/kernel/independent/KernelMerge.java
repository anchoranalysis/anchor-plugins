package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.kernel.independent;

import java.util.Optional;

import org.anchoranalysis.anchor.mpp.bean.init.MPPInitParams;
import org.anchoranalysis.anchor.mpp.bean.proposer.MarkMergeProposer;
import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMap;
import org.anchoranalysis.anchor.mpp.feature.mark.ListUpdatableMarkSetCollection;
import org.anchoranalysis.anchor.mpp.feature.mark.MemoList;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.set.UpdateMarkSetException;
import org.anchoranalysis.anchor.mpp.pair.Pair;
import org.anchoranalysis.anchor.mpp.pair.PairCollection;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.PxlMarkMemo;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.PxlMarkMemoFactory;

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
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.mpp.sgmn.bean.kernel.KernelPosNeg;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcContext;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcNRGException;

public class KernelMerge extends KernelPosNeg<CfgNRGPixelized> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5751099600940897752L;

	// START BEAN PROPERTIES
	@BeanField
	private MarkMergeProposer mergeMarkProposer = null;
	
	@BeanField
	private String simplePairCollectionID;
	// END BEAN PROPERTIES

	private Pair<Mark> pair;
	private Optional<Mark> markAdded;
	
	private enum FailedProposalType {
		NO_PAIRS,
		CANNOT_GENERATE_MERGE
	}
	
	@SuppressWarnings("unused")
	private FailedProposalType failedProposalType;
	
	private transient PairCollection<Pair<Mark>> pairCollection;

	@Override
	public void onInit(MPPInitParams pso) throws InitException {
		super.onInit(pso);
		
		// new SimplePairCollection( new AddCriteriaDistanceTo(maximumDistance)  
		
		try {
			pairCollection = getSharedObjects().getSimplePairCollection().getException(simplePairCollectionID);
		} catch (NamedProviderGetException e) {
			throw new InitException(e.summarize());
		}
		
		if (pairCollection==null) {
			throw new InitException(String.format("pairCollection '%s' not found",simplePairCollectionID));
		}
	}
	
	@Override
	public Optional<CfgNRGPixelized> makeProposal(Optional<CfgNRGPixelized> exst, KernelCalcContext context) throws KernelCalcNRGException {
		
		if (exst.isPresent()) {
			return Optional.empty();
		}
		
		ProposerContext propContext = context.proposer();
		
		pair = pairCollection.randomPairNonUniform( propContext.getRandomNumberGenerator() );
		if (pair==null) {
			failedProposalType = FailedProposalType.NO_PAIRS;
			markAdded = Optional.empty();
			propContext.getErrorNode().add("cannot generate pair");
			return Optional.empty();
		}
		
		PxlMarkMemo pmmSrc = exst.get().getMemoForMark(pair.getSource());
		PxlMarkMemo pmmDest = exst.get().getMemoForMark(pair.getDestination());
				
		// How and why does this happen?
		if (pmmSrc==null||pmmDest==null) {
			pair = null;
			markAdded = Optional.empty();
			failedProposalType = FailedProposalType.CANNOT_GENERATE_MERGE;
			return Optional.empty();
		}
		
		try {
			markAdded = mergeMarkProposer.propose(
				pmmSrc,
				pmmDest,
				context.proposer()
			);
		} catch (ProposalAbnormalFailureException e) {
			throw new KernelCalcNRGException(
				"Failed to propose a mark for merging due to abnormal exception",
				e
			);
		}				
		
		// If we can't generate a successful merge, we cancel the kernel
		if (!markAdded.isPresent()) {
			pair = null;
			markAdded = null;
			failedProposalType = FailedProposalType.CANNOT_GENERATE_MERGE;
			return Optional.empty();
		}
		
		markAdded.get().setId(
			context.cfgGen().getCfgGen().idAndIncrement()
		);

		return Optional.of(
			createCfgNRG(
				markAdded.get(),
				exst.get(),
				propContext.getNrgStack(),
				propContext.getRegionMap()
			)
		);
	}
	
	private CfgNRGPixelized createCfgNRG(Mark mark, CfgNRGPixelized exst, NRGStackWithParams nrgStack, RegionMap regionMap ) throws KernelCalcNRGException {
		
		// we need to get indexes for each mark (well make this tidier)
		int srcIndex = exst.getCfg().indexOf( pair.getSource() );
		int destIndex = exst.getCfg().indexOf( pair.getDestination() );
		assert srcIndex >= 0;
		assert destIndex >= 0;
		
		// We calculate a new NRG by exchanging our marks
		CfgNRGPixelized newNRG = exst.shallowCopy();

		try {
			newNRG.rmvTwo( srcIndex, destIndex, nrgStack.getNrgStack() );
		} catch ( FeatureCalcException e ) {
			throw new KernelCalcNRGException(
				String.format("Cannot remove indexes %d and %d", srcIndex, destIndex ),
				e
			);
		}
		
		PxlMarkMemo pmm = PxlMarkMemoFactory.create(
			mark,
			nrgStack.getNrgStack(),
			regionMap
		);
		
		try {
			newNRG.add( pmm, nrgStack.getNrgStack() );
		} catch (FeatureCalcException e) {
			throw new KernelCalcNRGException("Cannot add pmm", e);
		}

		return newNRG;
	}
	
	
	@Override
	public double calcAccptProb(int exstSize, int propSize,
			double poisson_intens, ImageDimensions scene_size, double densityRatio) {
		return densityRatio;
	}
	
	
	@Override
	public void updateAfterAccpt( ListUpdatableMarkSetCollection updatableMarkSetCollection, CfgNRGPixelized exst, CfgNRGPixelized accptd ) throws UpdateMarkSetException {
		
		MemoList memoList = exst.createDuplicatePxlMarkMemoList();
		
		int rmvIndex1 = exst.getCfg().indexOf( pair.getSource() );
		int rmvIndex2 = exst.getCfg().indexOf( pair.getDestination() );
		
		PxlMarkMemo memoSource = exst.getMemoForMark( pair.getSource() );
		PxlMarkMemo memoDest = exst.getMemoForMark( pair.getDestination() );
		
		// We need to delete in the correct order
		if (rmvIndex2 > rmvIndex1) {
			updatableMarkSetCollection.rmv( memoList, memoDest );
			memoList.remove(rmvIndex2);
		
			updatableMarkSetCollection.rmv( memoList, memoSource );
			memoList.remove(rmvIndex1);
		} else {
			updatableMarkSetCollection.rmv( memoList, memoSource );
			memoList.remove(rmvIndex1);
			
			updatableMarkSetCollection.rmv( memoList, memoDest );
			memoList.remove(rmvIndex2);
		}
		
		PxlMarkMemo memoAdded = accptd.getMemoForMark( markAdded.get() );
		
		// Should always find one
		assert memoAdded!=null;
		
		updatableMarkSetCollection.add( memoList, memoAdded );
	}

	@Override
	public String dscrLast() {
		if (pair != null && pair.getSource()!=null && pair.getDestination() !=null) {
			return String.format(
				"merge %d and %d into %d",
				pair.getSource().getId(),
				pair.getDestination().getId(),
				markAdded.map(Mark::getId).orElse(-1)
			);
		} else {
			return "merge";
		}
	}
	
	@Override
	public int[] changedMarkIDArray() {
		return new int[]{
			pair.getSource().getId(),
			pair.getDestination().getId(),
			markAdded.map(Mark::getId).orElse(-1)
		};
	}


	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return mergeMarkProposer.isCompatibleWith(testMark);
	}

	public MarkMergeProposer getMergeMarkProposer() {
		return mergeMarkProposer;
	}

	public void setMergeMarkProposer(MarkMergeProposer mergeMarkProposer) {
		this.mergeMarkProposer = mergeMarkProposer;
	}

	public PairCollection<Pair<Mark>> getPairCollection() {
		return pairCollection;
	}

	public void setPairCollection(PairCollection<Pair<Mark>> pairCollection) {
		this.pairCollection = pairCollection;
	}

	public String getSimplePairCollectionID() {
		return simplePairCollectionID;
	}

	public void setSimplePairCollectionID(String simplePairCollectionID) {
		this.simplePairCollectionID = simplePairCollectionID;
	}
}
