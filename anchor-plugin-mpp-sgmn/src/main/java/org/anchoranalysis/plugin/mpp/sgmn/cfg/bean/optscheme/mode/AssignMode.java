package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.mode;

import org.anchoranalysis.anchor.mpp.bean.anneal.AnnealScheme;

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

import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.mpp.sgmn.kernel.KernelAssigner;
import org.anchoranalysis.mpp.sgmn.optscheme.ExtractScoreSize;
import org.anchoranalysis.mpp.sgmn.optscheme.StateReporter;
import org.anchoranalysis.mpp.sgmn.transformer.TransformationContext;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.kernelbridge.KernelStateBridge;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.kernel.assigner.KernelAssignerCalcNRGFromKernel;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.optscheme.AccptProbCalculator;

/**
 * How assignments and other decisions are made in the SimulatedAnnealing optimizaton
 * 
 * @author FEEHANO
 *
 * @param <S> reporting back type
 * @param <T> state-type for optimization
*  @param <U> target-type for kernel assignment
 */
public abstract class AssignMode<S,T,U> extends AnchorBean<AssignMode<S,T,U>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public abstract AccptProbCalculator<T> probCalculator( AnnealScheme annealScheme );
	
	public KernelAssigner<U,T> kernelAssigner(TransformationContext tc) {
		return new KernelAssignerCalcNRGFromKernel<U,T>(
			kernelStateBridge()
		);
	}
	
	public abstract KernelStateBridge<U,T> kernelStateBridge();
	
	public abstract StateReporter<T,S> stateReporter();
		
	public abstract ExtractScoreSize<S> extractScoreSizeReport();
	
	public abstract ExtractScoreSize<T> extractScoreSizeState();
}
