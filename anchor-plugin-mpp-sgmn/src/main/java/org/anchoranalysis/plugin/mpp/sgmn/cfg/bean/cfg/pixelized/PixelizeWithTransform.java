package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.cfg.pixelized;

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
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.mpp.sgmn.transformer.StateTransformerBean;
import org.anchoranalysis.mpp.sgmn.transformer.TransformationContext;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.optscheme.ToPixelized;


/**
 * Converts a Cfg to a CfgToPixelized using a transformer
 * @author FEEHANO
 *
 */
public class PixelizeWithTransform<T> extends StateTransformerBean<T,ToPixelized<T>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private StateTransformerBean<T,CfgNRGPixelized> transformer;
	// END BEAN PROPERTIES
	
	public PixelizeWithTransform() {
		
	}
	
	public PixelizeWithTransform(StateTransformerBean<T, CfgNRGPixelized> transformer) {
		super();
		this.transformer = transformer;
	}

		
	@Override
	public ToPixelized<T> transform(T in, TransformationContext context)
			throws OperationFailedException {
		return new ToPixelized<>(
			in,
			transformer.transform(in, context)
		);
	}

	public StateTransformerBean<T, CfgNRGPixelized> getTransformer() {
		return transformer;
	}

	public void setTransformer(StateTransformerBean<T, CfgNRGPixelized> transformer) {
		this.transformer = transformer;
	}


}
