package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.kernel.dependent.mark;

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

import java.util.Optional;
import java.util.Set;

import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.feature.mark.ListUpdatableMarkSetCollection;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.set.UpdateMarkSetException;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcContext;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcNRGException;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.kernel.independent.KernelBirth;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.optscheme.CfgFromPartition;


/**
 * Proposes new marks ONLY if they haven't already been proposed and accepted.
 * 
 * <p>As an example, this is like sampling WITHOUT replacement.</p>
 * 
 * @author Owen Feehan
 *
 */
public class KernelBirthPartition extends KernelBirth<CfgFromPartition> {

	@Override
	protected Optional<Set<Mark>> proposeNewMarks(CfgFromPartition exst, int number, KernelCalcContext context) {
		assert( exst!= null);
		assert( exst.getPartition()!= null);
		return exst.getPartition().sampleFromAvailable(number);
	}
	
	@Override
	protected Optional<CfgFromPartition> calcForNewMark(CfgFromPartition exst, Set<Mark> listMarksNew, KernelCalcContext context)
			throws KernelCalcNRGException {

		if (listMarksNew==null || listMarksNew.isEmpty()) {
			return Optional.empty();
		}
		
		Cfg cfg = exst.getCfg();
		for( Mark m : listMarksNew) {
			cfg = calcUpdatedCfg(cfg, m);
		}
		
		return Optional.of(
			exst.copyChange(cfg)
		);
	}

	@Override
	public void updateAfterAccpt(ListUpdatableMarkSetCollection updatableMarkSetCollection, CfgFromPartition nrgExst,
			CfgFromPartition nrgNew) throws UpdateMarkSetException {
		if (getMarkNew().isPresent()) {
			nrgNew.getPartition().moveAvailableToAccepted( getMarkNew().get() );
		}
	}

	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return true;
	}
	
	private static Cfg calcUpdatedCfg( Cfg exst, Mark mark ) {
		Cfg newCfg = exst.shallowCopy();
		newCfg.add(mark);
		return newCfg;
	}
}
