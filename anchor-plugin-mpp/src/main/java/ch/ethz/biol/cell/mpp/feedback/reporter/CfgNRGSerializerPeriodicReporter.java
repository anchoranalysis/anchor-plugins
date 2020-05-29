package ch.ethz.biol.cell.mpp.feedback.reporter;

import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRG;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;

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
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.ReporterException;
import org.anchoranalysis.mpp.sgmn.optscheme.step.Reporting;

public class CfgNRGSerializerPeriodicReporter extends ObjectSerializerPeriodicReporter<CfgNRG> {

	// BEAN PARAMETERS
	@BeanField
	private boolean proposal = false;
	
	/** If proposal==true, this toggles between the primary and secondary proposal */
	@BeanField
	private boolean secondary = false;
	// END BEAN PARAMETERS
	
	public CfgNRGSerializerPeriodicReporter() {
		super("cfgNRG");
	}
	
	@Override
	protected CfgNRG generateIterableElement( Reporting<CfgNRGPixelized> reporting ) throws ReporterException {
		
		if (proposal) {
			return proposalCfgNRGOrNull(reporting);
		} else {
			return reporting.getCfgNRGAfter().getCfgNRG();
		}
	}
	
	
	private CfgNRG proposalCfgNRGOrNull( Reporting<CfgNRGPixelized> reporting ) {
		
		CfgNRGPixelized p = secondary ? reporting.getProposalSecondary() : reporting.getProposal();
		if (p!=null) {
			return p.getCfgNRG();
		} else {
			return null;
		}
	}
	
	public boolean isProposal() {
		return proposal;
	}

	public void setProposal(boolean proposal) {
		this.proposal = proposal;
	}
	
	@Override
	public void reportNewBest(Reporting<CfgNRGPixelized> reporting) {
	}

	public boolean isSecondary() {
		return secondary;
	}

	public void setSecondary(boolean secondary) {
		this.secondary = secondary;
	}
}
