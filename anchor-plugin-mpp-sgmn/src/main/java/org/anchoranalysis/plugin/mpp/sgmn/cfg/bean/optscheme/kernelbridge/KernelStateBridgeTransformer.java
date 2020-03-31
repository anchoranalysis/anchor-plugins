package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.kernelbridge;

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
import org.anchoranalysis.mpp.sgmn.transformer.StateTransformer;
import org.anchoranalysis.mpp.sgmn.transformer.StateTransformerBean;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.mode.TransformIfNotNull;

public class KernelStateBridgeTransformer<U,T> extends KernelStateBridge<U, T> {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private StateTransformerBean<U,T> transformerKernelToState;
	
	@BeanField
	private StateTransformerBean<T,U> transformerStateToKernel;
	// END BEAN PROPERTIES
	
	@Override
	public StateTransformer<U, T> kernelToState() {
		return new TransformIfNotNull<>(transformerKernelToState);
	}

	@Override
	public StateTransformer<T, U> stateToKernel() {
		return new TransformIfNotNull<>(transformerStateToKernel);
	}

	public StateTransformerBean<U, T> getTransformerKernelToState() {
		return transformerKernelToState;
	}

	public void setTransformerKernelToState(StateTransformerBean<U, T> transformerKernelToState) {
		this.transformerKernelToState = transformerKernelToState;
	}

	public StateTransformerBean<T, U> getTransformerStateToKernel() {
		return transformerStateToKernel;
	}

	public void setTransformerStateToKernel(StateTransformerBean<T, U> transformerStateToKernel) {
		this.transformerStateToKernel = transformerStateToKernel;
	}
}
