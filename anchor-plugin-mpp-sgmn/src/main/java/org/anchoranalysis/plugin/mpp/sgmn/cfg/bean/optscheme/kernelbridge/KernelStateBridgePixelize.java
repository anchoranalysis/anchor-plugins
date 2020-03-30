package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.kernelbridge;

import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;

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
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.cfg.pixelized.PixelizeWithTransform;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.cfg.pixelized.RetrieveSourceFromPixelized;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.mode.TransformIfNotNull;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.optscheme.ToPixelized;

/**
 * State takes the form of ToPixelized<T> derived from the Kernel
 * 
 * @author FEEHANO
 *
 * @param <U>
 * @param <T>
 */
public class KernelStateBridgePixelize<T> extends KernelStateBridge<T, ToPixelized<T>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private StateTransformerBean<T,CfgNRGPixelized> transformer;
	// END BEAN PROPERTIES

	@Override
	public StateTransformer<T, ToPixelized<T>> kernelToState() {
		return new TransformIfNotNull<>( 
			new PixelizeWithTransform<>(transformer)
		);
	}

	@Override
	public StateTransformer<ToPixelized<T>, T> stateToKernel() {
		return new TransformIfNotNull<>( 
			new RetrieveSourceFromPixelized<>()
		);
	}

	public StateTransformerBean<T, CfgNRGPixelized> getTransformer() {
		return transformer;
	}

	public void setTransformer(StateTransformerBean<T, CfgNRGPixelized> transformer) {
		this.transformer = transformer;
	}

}