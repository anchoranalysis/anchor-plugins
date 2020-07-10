package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.kernel.independent.pixelized;

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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.anchoranalysis.anchor.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.anchor.mpp.feature.mark.ListUpdatableMarkSetCollection;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.set.UpdateMarkSetException;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.VoxelizedMarkMemo;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcContext;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcNRGException;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.kernel.independent.KernelBirth;

/**
 * 
 * <p>As an example, this is like sampling WITH replacement.</p>
 * 
 * @author Owen Feehan
 *
 */
public class KernelBirthPixelized extends KernelBirth<CfgNRGPixelized> {

	// START BEAN PROPERTIES
	@BeanField
	private MarkProposer markProposer;
	// END BEAN PROPERTIES

	public KernelBirthPixelized() {
		// Standard bean constructor
	}
	
	public KernelBirthPixelized(MarkProposer markProposer) {
		this.markProposer = markProposer;
	}
	
	@Override
	protected Optional<Set<Mark>> proposeNewMarks(CfgNRGPixelized exst, int number, KernelCalcContext context) {
		
		Set<Mark> out = new HashSet<>();
		for( int i=0; i<number; i++) {
			out.add(
				proposeNewMark(context)
			);
		}
		return Optional.of(out);
	}
	
	@Override
	protected Optional<CfgNRGPixelized> calcForNewMark(
		CfgNRGPixelized exst,
		Set<Mark> listMarksNew,
		KernelCalcContext context
	) throws KernelCalcNRGException {
		
		ProposerContext propContext = context.proposer();
		
		Optional<CfgNRGPixelized> pixelized = Optional.of(exst);
		
		for( Mark m : listMarksNew ) {
			pixelized = OptionalUtilities.flatMap( 
				pixelized,
				p -> proposeAndUpdate(p, m, propContext)
			);
		}
						
		return pixelized;		
	}

	@Override
	public void updateAfterAccpt(
		ListUpdatableMarkSetCollection updatableMarkSetCollection,
		CfgNRGPixelized exst,
		CfgNRGPixelized accptd
	) throws UpdateMarkSetException {
		
		for( Mark m : getMarkNew().get() ) {
			VoxelizedMarkMemo memo = accptd.getMemoForMark( m );
			exst.addToUpdatablePairList( updatableMarkSetCollection, memo );
		}
	}

	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return markProposer.isCompatibleWith(testMark);
	}
	
	private Optional<CfgNRGPixelized> proposeAndUpdate( CfgNRGPixelized exst, Mark markNew, ProposerContext propContext) throws KernelCalcNRGException {

		VoxelizedMarkMemo pmmMarkNew = propContext.create(markNew );
		
		if (!applyMarkProposer(pmmMarkNew, propContext)) {
			return Optional.empty();
		}
				
		return Optional.of(
			calcUpdatedNRG(exst, pmmMarkNew, propContext)
		);
	}
	
	private boolean applyMarkProposer( VoxelizedMarkMemo pmmMarkNew, ProposerContext context ) throws KernelCalcNRGException {
		try {
			return markProposer.propose(pmmMarkNew, context);
		} catch (ProposalAbnormalFailureException e) {
			throw new KernelCalcNRGException(
				"Failed to propose a mark due to abnormal exception",
				e
			);
		}
	}
	
	private static CfgNRGPixelized calcUpdatedNRG( CfgNRGPixelized exst, VoxelizedMarkMemo pmmMark, ProposerContext propContext ) throws KernelCalcNRGException {
		
		// We calculate a new NRG by adding our marks
		CfgNRGPixelized newNRG = exst.deepCopy();
		try {
			assert( pmmMark!=null );
			newNRG.add(
				pmmMark,
				propContext.getNrgStack().getNrgStack()
			);
			
		} catch (FeatureCalcException e) {
			throw new KernelCalcNRGException("Cannot add pmmMarkNew", e);
		}
		
		return newNRG;		
	}

	public MarkProposer getMarkProposer() {
		return markProposer;
	}

	public void setMarkProposer(MarkProposer markProposer) {
		this.markProposer = markProposer;
	}
	
	private Mark proposeNewMark(KernelCalcContext context) {
		return context.cfgGen().getCfgGen().newTemplateMark();
	}
}
