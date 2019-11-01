package ch.ethz.biol.cell.mpp.cfg.proposer;

import org.anchoranalysis.anchor.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.anchor.mpp.mark.Mark;
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

import anchor.provider.bean.ProposalAbnormalFailureException;
import cern.jet.random.Poisson;
import ch.ethz.biol.cell.mpp.cfg.Cfg;
import ch.ethz.biol.cell.mpp.cfg.CfgGen;

public class CfgProposerSimple extends CfgProposer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1124249102380649830L;

	// START BEAN
	@BeanField
	private MarkProposer markProposer;
	
	@BeanField
	private int numMarks = -1;	// if positive, then we use this as the definitive number of marks 
	// END BEAN
	
	public CfgProposerSimple() {
		super();
	}
	
	// Generates a random configuration
	//   * Number of objects is decided by a Poisson distribution
	//	 * The location and attributes of marks are uniformly distributed
	@Override
	public Cfg propose( CfgGen cfgGen, ProposerContext context) throws ProposalAbnormalFailureException {

		int num_pts;
		if (numMarks>0) {
			num_pts = numMarks;
		} else {
		
			Poisson p = context.getRe().generatePossion( cfgGen.getReferencePoissonIntensity() * context.getDimensions().getVolume() );

			//do {
			num_pts = p.nextInt();
			//} while (num_pts==0);
		}
		
		return genInitRndCfgForNumPts( num_pts, cfgGen, context );
	}
	

	// Generates a random configuration for a given number of points
	//	 * The location and attributes of marks are uniformly distributed
	private Cfg genInitRndCfgForNumPts( int num_pts, CfgGen cfgGen, ProposerContext context ) throws ProposalAbnormalFailureException {
		
		Cfg cfg = new Cfg();
		
		for (int i=0; i<num_pts; i++) {
			Mark m = cfgGen.newTemplateMark();
			
			PxlMarkMemo pmm = context.create(m );
			
			// If the proposal fails, we don't bother trying another
			if (markProposer.propose(pmm, context )) {
				cfg.add( m );	
			}
			
			pmm = null;
		}
		
		return cfg;
	}

	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return this.markProposer.isCompatibleWith(testMark);
	}

	@Override
	public String getBeanDscr() {
		return getBeanName();
	}

	public MarkProposer getMarkProposer() {
		return markProposer;
	}

	public void setMarkProposer(MarkProposer markProposer) {
		this.markProposer = markProposer;
	}

	public int getNumMarks() {
		return numMarks;
	}

	public void setNumMarks(int numMarks) {
		this.numMarks = numMarks;
	}

}
