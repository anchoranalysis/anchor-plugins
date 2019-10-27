package org.anchoranalysis.plugin.mpp.sgmn.bean.kernel.independent.pixelized;

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
import java.util.Set;

import org.anchoranalysis.anchor.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcContext;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcNRGException;
import org.anchoranalysis.plugin.mpp.sgmn.bean.kernel.independent.KernelBirth;

import anchor.provider.bean.ProposalAbnormalFailureException;
import ch.ethz.biol.cell.mpp.mark.Mark;
import ch.ethz.biol.cell.mpp.mark.pxlmark.memo.PxlMarkMemo;
import ch.ethz.biol.cell.mpp.nrg.CfgNRGPixelized;
import ch.ethz.biol.cell.mpp.pair.ListUpdatableMarkSetCollection;
import ch.ethz.biol.cell.mpp.pair.UpdateMarkSetException;

public class KernelBirthPixelized extends KernelBirth<CfgNRGPixelized> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private MarkProposer markProposer = null;
	// END BEAN PROPERTIES

	@Override
	public void updateAfterAccpt(
			ListUpdatableMarkSetCollection updatableMarkSetCollection, CfgNRGPixelized exst, CfgNRGPixelized accptd) throws UpdateMarkSetException {
		
		for( Mark m : getMarkNew() ) {
			PxlMarkMemo memo = accptd.getMemoForMark( m );
			exst.addToUpdatablePairList( updatableMarkSetCollection, memo );
		}
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
	
	

	@Override
	protected Set<Mark> proposeNewMarks(CfgNRGPixelized exst, int number, KernelCalcContext context) {
		
		Set<Mark> out = new HashSet<>();
		for( int i=0; i<number; i++) {
			Mark m = proposeNewMark(exst, context);
			if (m!=null) {
				out.add(m);
			}
		}
		return out;
	}
	
	private Mark proposeNewMark(CfgNRGPixelized exst, KernelCalcContext context) {
		return context.cfgGen().getCfgGen().newTemplateMark();
	}
	
	@Override
	protected CfgNRGPixelized calcForNewMark( CfgNRGPixelized exst, Set<Mark> listMarksNew, KernelCalcContext context ) throws KernelCalcNRGException {
		
		ProposerContext propContext = context.proposer();
		
		CfgNRGPixelized pixelized = exst;
		for( Mark m : listMarksNew ) {
			pixelized = proposeAndUpdate(pixelized, m, propContext);
			
		}
						
		return pixelized;		
	}
	
	private CfgNRGPixelized proposeAndUpdate( CfgNRGPixelized exst, Mark markNew, ProposerContext propContext) throws KernelCalcNRGException {

		PxlMarkMemo pmmMarkNew = propContext.create(markNew );
		
		if (!applyMarkProposer(pmmMarkNew, propContext)) {
			return null;
		}
				
		return calcUpdatedNRG(exst, pmmMarkNew, propContext);	
	}
	
	private boolean applyMarkProposer( PxlMarkMemo pmmMarkNew, ProposerContext context ) throws KernelCalcNRGException {
		try {
			if ( !markProposer.propose(pmmMarkNew, context) ) {
				return false;
			}
			return true;
		} catch (ProposalAbnormalFailureException e) {
			throw new KernelCalcNRGException(
				"Failed to propose a mark due to abnormal exception",
				e
			);
		}
	}
	
	private static CfgNRGPixelized calcUpdatedNRG( CfgNRGPixelized exst, PxlMarkMemo pmmMark, ProposerContext propContext ) throws KernelCalcNRGException {
		
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

}
