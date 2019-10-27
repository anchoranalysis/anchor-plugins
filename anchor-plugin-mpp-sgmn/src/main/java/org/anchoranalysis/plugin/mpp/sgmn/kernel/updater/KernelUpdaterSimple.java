package org.anchoranalysis.plugin.mpp.sgmn.kernel.updater;

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

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.mpp.sgmn.bean.kernel.Kernel;
import org.anchoranalysis.mpp.sgmn.cfgnrg.transformer.StateTransformer;
import org.anchoranalysis.mpp.sgmn.kernel.proposer.WeightedKernel;
import org.anchoranalysis.mpp.sgmn.kernel.proposer.WeightedKernelList;
import org.anchoranalysis.plugin.mpp.sgmn.optscheme.TransformationContext;

import ch.ethz.biol.cell.mpp.pair.ListUpdatableMarkSetCollection;
import ch.ethz.biol.cell.mpp.pair.UpdateMarkSetException;

public class KernelUpdaterSimple<S,T> extends KernelUpdater<S,T> {

	private ListUpdatableMarkSetCollection updatableMarkSetCollection;
	private WeightedKernelList<S> allKernels;
	private StateTransformer<T,S> funcExtractCfgNRG;
	
	public KernelUpdaterSimple(
		ListUpdatableMarkSetCollection updatableMarkSetCollection,
		WeightedKernelList<S> allKernels,
		StateTransformer<T,S> funcExtractCfgNRG
	) {
		super();
		this.updatableMarkSetCollection = updatableMarkSetCollection;
		this.allKernels = allKernels;
		this.funcExtractCfgNRG = funcExtractCfgNRG;
	}	
	
	@Override
	public void kernelAccepted( Kernel<S> kernel, T crnt, T proposed, TransformationContext context ) throws UpdateMarkSetException {
		
		try {
			S crntConv = funcExtractCfgNRG.transform(crnt, context);
			S proposedConv = funcExtractCfgNRG.transform(proposed, context);
			
			updateAfterAccept(kernel, crntConv, proposedConv);
			
			informAllKernelsAfterAccept( proposedConv );
			
		} catch (OperationFailedException e) {
			throw new UpdateMarkSetException(e);
		}
	}
	
	private void updateAfterAccept(Kernel<S> kernel, S crnt, S proposed)
			throws UpdateMarkSetException {
		kernel.updateAfterAccpt( updatableMarkSetCollection, crnt, proposed );
	}

	private void informAllKernelsAfterAccept(S cfgNRG) {
		// We inform ALL kernels of the new NRG
		for( WeightedKernel<S> wk : allKernels ) {
			// INFORM KERNEL
			wk.getKernel().informLatestState( cfgNRG );
		}
	}
}
