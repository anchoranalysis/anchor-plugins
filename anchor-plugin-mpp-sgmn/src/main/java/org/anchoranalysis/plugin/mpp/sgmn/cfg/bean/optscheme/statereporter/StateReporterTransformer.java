package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.statereporter;

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
import org.anchoranalysis.bean.annotation.Optional;
import org.anchoranalysis.mpp.sgmn.optscheme.StateReporter;
import org.anchoranalysis.mpp.sgmn.transformer.StateTransformer;
import org.anchoranalysis.mpp.sgmn.transformer.StateTransformerBean;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.mode.TransformIfNotNull;

public class StateReporterTransformer<T,S> extends StateReporter<T, S> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private StateTransformerBean<T,S> transformerPrimary;
	
	@BeanField @Optional
	private StateTransformerBean<T,S> transformerSecondary;
	// END BEAN PROPERTIES

	@Override
	public StateTransformer<T, S> primaryReport() {
		return new TransformIfNotNull<>(transformerPrimary);
	}

	@Override
	public boolean hasSecondaryReport() {
		return transformerSecondary!=null;
	}	
	
	@Override
	public StateTransformer<T, S> secondaryReport() {
		return new TransformIfNotNull<>(transformerSecondary);
	}

	public StateTransformerBean<T, S> getTransformerPrimary() {
		return transformerPrimary;
	}

	public void setTransformerPrimary(StateTransformerBean<T, S> transformerPrimary) {
		this.transformerPrimary = transformerPrimary;
	}

	public StateTransformerBean<T, S> getTransformerSecondary() {
		return transformerSecondary;
	}

	public void setTransformerSecondary(StateTransformerBean<T, S> transformerSecondary) {
		this.transformerSecondary = transformerSecondary;
	}
}
