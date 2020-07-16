package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.kernel.independent;

/*-
 * #%L
 * anchor-plugin-mpp-sgmn
 * %%
 * Copyright (C) 2010 - 2019 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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
import java.util.Optional;

import org.anchoranalysis.anchor.mpp.bean.cfg.CfgGen;
import org.anchoranalysis.anchor.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.MarkAbstractPosition;
import org.anchoranalysis.anchor.mpp.overlap.OverlapUtilities;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.VoxelizedMarkMemo;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcContext;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcNRGException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
class KernelBirthAndKillHelper {
	
	public static VoxelizedMarkMemo makeAdditionalBirth(
		MarkProposer markProposerAdditionalBirth,
		Mark markNew,
		List<VoxelizedMarkMemo> toKill,
		CfgGen cfgGen,
		KernelCalcContext context
	) throws KernelCalcNRGException {
		
		VoxelizedMarkMemo pmmAdditional = null;
					
		PositionProposerMemoList pp = new PositionProposerMemoList( toKill, markNew );
		
		ProposerContext propContext = context.proposer();
		
		Optional<Point3d> pointAdditionalBirth = pp.propose( propContext );
	
		if (pointAdditionalBirth.isPresent()) {
			MarkAbstractPosition markNewAdditional = (MarkAbstractPosition) cfgGen.newTemplateMark();
			markNewAdditional.setPos( pointAdditionalBirth.get() );
						
			pmmAdditional = propContext.create(markNewAdditional);
					
			try {
				markProposerAdditionalBirth.propose( pmmAdditional, context.proposer());
			} catch (ProposalAbnormalFailureException e) {
				throw new KernelCalcNRGException(
					"Failed to propose an additional-mark due to abnormal exception",
					e
				);
			}					
		}

		return pmmAdditional;
	}
	
	public static List<VoxelizedMarkMemo> determineKillObjects(
		VoxelizedMarkMemo memoNew,
		CfgNRGPixelized exst,
		int regionID,
		double overlapRatioThreshold
	) throws OperationFailedException {
		
		List<VoxelizedMarkMemo> outList = new ArrayList<>();
		
		// We always measure overlap in terms of the object being added
		long numPixelsNew = memoNew.voxelized().statisticsForAllSlices(0, regionID).size();
		
		for( int i=0; i<exst.getCfg().size(); i++) {
			VoxelizedMarkMemo memoExst = exst.getMemoForIndex(i);
			
			long numPixelsExst = memoExst.voxelized().statisticsForAllSlices(0, regionID).size();
			
			double overlap = OverlapUtilities.overlapWith(memoExst, memoNew,regionID);
			
			if (numPixelsNew==0) {
				outList.add( memoExst );
				continue;
			}
			
			double overlapRatio = Math.max( overlap / numPixelsNew, overlap / numPixelsExst );
			
			if (Double.isNaN(overlapRatio)) {
				throw new OperationFailedException( "Kill objects has NaN" );
			}
			
			if (Double.isInfinite(overlapRatio)) {
				throw new OperationFailedException( "Kill objects has Infinite" );
			}
			
			if (overlapRatio > overlapRatioThreshold) {
				
				outList.add( memoExst );
			}
		}
		return outList;				
	}
	
	public static CfgNRGPixelized calcUpdatedNRG(
		CfgNRGPixelized exst,			
		VoxelizedMarkMemo memoNew,
		VoxelizedMarkMemo pmmAdditional,	// Can be NULL
		List<VoxelizedMarkMemo> toKill,
		ProposerContext propContext
	) throws KernelCalcNRGException {

		// Now we add what we need to add, and remove what we need to remove from the NRG
		
		// Now we remove each item that we need to remove
		CfgNRGPixelized newNRG = exst.shallowCopy();
		assert !Double.isNaN(newNRG.getTotal());
		
		try {
			newNRG.add( memoNew, propContext.getNrgStack().getNrgStack() );
			assert !Double.isNaN(newNRG.getTotal());
		} catch ( FeatureCalcException e ) {
			throw new KernelCalcNRGException("Cannot add memoNew", e);
		}	
			
		for( VoxelizedMarkMemo memo : toKill) {
			try {
				newNRG.rmv(memo, propContext.getNrgStack().getNrgStack() );
				assert !Double.isNaN( newNRG.getTotal() ); 
			} catch ( FeatureCalcException e ) {
				throw new KernelCalcNRGException("Cannot remove memo", e);
			}					
		}
		
		if (pmmAdditional!=null) {
		
			try {
				newNRG.add(pmmAdditional, propContext.getNrgStack().getNrgStack() );
			} catch ( FeatureCalcException e ) {
				throw new KernelCalcNRGException("Cannot add pmmAdditional", e);
			}	
		}
		
		assert !Double.isNaN(newNRG.getTotal());
	
		return newNRG;	
	}
}
