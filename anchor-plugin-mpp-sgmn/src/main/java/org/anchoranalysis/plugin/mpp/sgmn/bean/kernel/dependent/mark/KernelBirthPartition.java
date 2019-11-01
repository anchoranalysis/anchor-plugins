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

import java.util.HashSet;
import java.util.Set;

import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcContext;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcNRGException;
import org.anchoranalysis.plugin.mpp.sgmn.bean.kernel.independent.KernelBirth;
import org.anchoranalysis.plugin.mpp.sgmn.optscheme.CfgFromPartition;
import org.apache.commons.collections.CollectionUtils;

import ch.ethz.biol.cell.mpp.cfg.Cfg;
import ch.ethz.biol.cell.mpp.pair.ListUpdatableMarkSetCollection;
import ch.ethz.biol.cell.mpp.pair.UpdateMarkSetException;

public class KernelBirthPartition extends KernelBirth<CfgFromPartition> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return true;
	}

	@Override
	protected Set<Mark> proposeNewMarks(CfgFromPartition exst, int number, KernelCalcContext context) {
		assert( exst!= null);
		assert( exst.getPartition()!= null);
		
		Mark[] arr = new Mark[number];
		
		
		if (exst.getPartition().sampleAvailable(context.proposer(), number, arr)) {
			return convertArrToSet(arr);
		} else {
			return null;
		}
	}
	
	@Override
	protected CfgFromPartition calcForNewMark(CfgFromPartition exst, Set<Mark> listMarksNew, KernelCalcContext context)
			throws KernelCalcNRGException {

		if (listMarksNew==null || listMarksNew.size()==0) {
			return null;
		}
		
		Cfg cfg = exst.getCfg();
		for( Mark m : listMarksNew) {
			cfg = calcUpdatedCfg(cfg, m);
		}
		
		return exst.copyChange(cfg);
	}

	@Override
	public void updateAfterAccpt(ListUpdatableMarkSetCollection updatableMarkSetCollection, CfgFromPartition nrgExst,
			CfgFromPartition nrgNew) throws UpdateMarkSetException {
		if (getMarkNew()!=null) {
			nrgNew.getPartition().moveAvailableToAccepted( getMarkNew() );
		}
	}

	private static Cfg calcUpdatedCfg( Cfg exst, Mark mark ) throws KernelCalcNRGException {
		Cfg newCfg = exst.shallowCopy();
		newCfg.add(mark);
		return newCfg;
	}
		
	private static Set<Mark> convertArrToSet( Mark[] arr ) {
		Set<Mark> set = new HashSet<Mark>();
		CollectionUtils.addAll(set, arr);
		return set;
	}
}
