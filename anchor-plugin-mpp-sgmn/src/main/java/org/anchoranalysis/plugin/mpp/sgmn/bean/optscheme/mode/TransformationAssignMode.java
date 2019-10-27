package org.anchoranalysis.plugin.mpp.sgmn.bean.optscheme.mode;

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

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.mpp.sgmn.optscheme.extractscoresize.ExtractScoreSize;
import org.anchoranalysis.plugin.mpp.sgmn.bean.optscheme.kernelbridge.KernelStateBridge;
import org.anchoranalysis.plugin.mpp.sgmn.bean.optscheme.statereporter.StateReporter;
import org.anchoranalysis.plugin.mpp.sgmn.optscheme.AccptProbCalculator;

import ch.ethz.biol.cell.mpp.anneal.AnnealScheme;


/**
 * Applies a transformation to the kernel-type U to calculate the NRG used as the primary readout during optimization
 * 
 * However, the kernel manipulation layer will always function in terms of the untransformed NRG (U)
 *   as the optimization continues
 *   
 * The final transformation, as well as what's "reported" out use the TRANSFORMED (S) version
 * 
 * @author FEEHANO
 *
 */
public class TransformationAssignMode<S,T,U> extends AssignMode<S,T,U> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private KernelStateBridge<U, T> kernelStateBridge;
	
	@BeanField
	private StateReporter<T,S> stateReporter;
	
	@BeanField
	private ExtractScoreSize<S> extractScoreSizeReport;
	
	@BeanField
	private ExtractScoreSize<T> extractScoreSizeState;
	// END BEAN PROPERTIES
	
	@Override
	public AccptProbCalculator<T> probCalculator(AnnealScheme annealScheme) {
		return new AccptProbCalculator<T>(
			annealScheme,
			extractScoreSizeState
		);
	}

	@Override
	public KernelStateBridge<U, T> kernelStateBridge() {
		return kernelStateBridge;
	}
	

	@Override
	public StateReporter<T,S> stateReporter() {
		return stateReporter;
	}

	
	@Override
	public ExtractScoreSize<S> extractScoreSizeReport() {
		return extractScoreSizeReport;
	}
	
	@Override
	public ExtractScoreSize<T> extractScoreSizeState() {
		return extractScoreSizeState;
	}
	
	
	
	public ExtractScoreSize<S> getExtractScoreSizeReport() {
		return extractScoreSizeReport;
	}

	public void setExtractScoreSizeReport(ExtractScoreSize<S> extractScoreSizeReport) {
		this.extractScoreSizeReport = extractScoreSizeReport;
	}

	public ExtractScoreSize<T> getExtractScoreSizeState() {
		return extractScoreSizeState;
	}

	public void setExtractScoreSizeState(ExtractScoreSize<T> extractScoreSizeState) {
		this.extractScoreSizeState = extractScoreSizeState;
	}


	public KernelStateBridge<U, T> getKernelStateBridge() {
		return kernelStateBridge;
	}


	public void setKernelStateBridge(KernelStateBridge<U, T> kernelStateBridge) {
		this.kernelStateBridge = kernelStateBridge;
	}

	public StateReporter<T,S> getStateReporter() {
		return stateReporter;
	}

	public void setStateReporter(StateReporter<T,S> stateReporter) {
		this.stateReporter = stateReporter;
	}
}
