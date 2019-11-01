package org.anchoranalysis.plugin.mpp.sgmn.bean.kernel.independent;

import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;

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

import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcContext;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcNRGException;

import ch.ethz.biol.cell.mpp.cfg.Cfg;
import ch.ethz.biol.cell.mpp.cfg.CfgGen;
import ch.ethz.biol.cell.mpp.cfg.proposer.CfgProposer;

class InitCfgUtilities {
	
	public static Cfg propose( CfgProposer cfgProposer, KernelCalcContext context ) throws KernelCalcNRGException {
		ProposerContext propContext = context.proposer();
		
		// We don't expect an existing exsting CfgNRG, but rather null (or whatever)
		
		// Initial cfg
		return proposeCfg( context.cfgGen().getCfgGen(), cfgProposer, propContext );
	}
	
	private static Cfg proposeCfg( CfgGen cfgGen, CfgProposer cfgProposer, ProposerContext propContext ) throws KernelCalcNRGException {
		try {
			return cfgProposer.propose( cfgGen, propContext );
		} catch (ProposalAbnormalFailureException e) {
			throw new KernelCalcNRGException(
				"Failed to propose an initial-cfg due to an abnormal error",
				e
			);
		}
	}
}
