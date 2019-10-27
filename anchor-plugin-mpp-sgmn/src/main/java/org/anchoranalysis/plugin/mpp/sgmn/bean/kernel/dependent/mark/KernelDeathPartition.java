package org.anchoranalysis.plugin.mpp.sgmn.bean.kernel.dependent.mark;

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
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcNRGException;
import org.anchoranalysis.plugin.mpp.sgmn.bean.kernel.independent.KernelDeath;
import org.anchoranalysis.plugin.mpp.sgmn.optscheme.CfgFromPartition;

import ch.ethz.biol.cell.mpp.cfg.Cfg;
import ch.ethz.biol.cell.mpp.mark.Mark;
import ch.ethz.biol.cell.mpp.pair.ListUpdatableMarkSetCollection;
import ch.ethz.biol.cell.mpp.pair.UpdateMarkSetException;

public class KernelDeathPartition extends KernelDeath<CfgFromPartition> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected MarkAnd<Mark, CfgFromPartition> removeAndUpdateNrg(CfgFromPartition exst, ProposerContext context)
			throws KernelCalcNRGException {

		int index = selectIndexToRmv( exst.getCfg(), context );
		
		if (index==-1) {
			return new MarkAnd<>(null,null);
		}
		
		Cfg cfgNew = exst.getCfg().shallowCopy();
		cfgNew.remove(index);
		return new MarkAnd<>(
			exst.getCfg().get(index),
			exst.copyChange(cfgNew)
		);
	}

	@Override
	public void updateAfterAccpt(ListUpdatableMarkSetCollection updatableMarkSetCollection, CfgFromPartition nrgExst,
			CfgFromPartition nrgNew) throws UpdateMarkSetException {
		nrgNew.getPartition().moveAcceptedToAvailable( getMarkRmv() );
	}
}
