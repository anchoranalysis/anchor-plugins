package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.statereporter;

import java.util.Optional;

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
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.mpp.sgmn.optscheme.StateReporter;
import org.anchoranalysis.mpp.sgmn.transformer.Compose;
import org.anchoranalysis.mpp.sgmn.transformer.StateTransformer;
import org.anchoranalysis.mpp.sgmn.transformer.StateTransformerBean;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.cfg.pixelized.RetrieveDestinationFromPixelized;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.cfg.pixelized.RetrieveSourceFromPixelized;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.optscheme.ToPixelized;

/**
 * Assumes we are interested in CfgNRGPixelized as reporting type, and our optimization-state
 *   is in the form ToPixelized<T>
 *   
 * @author Owen Feehan
 *
 * @param <T>
 */
public class StateReporterToPixelized<T> extends StateReporter<ToPixelized<T>, CfgNRGPixelized> {

	// START BEAN PROPERTIES
	@BeanField @OptionalBean
	private StateTransformerBean<T,CfgNRGPixelized> secondary;
	// END BEAN PROPERTIES
	
	@Override
	public StateTransformer<ToPixelized<T>, CfgNRGPixelized> primaryReport() {
		return new RetrieveDestinationFromPixelized<>();
	}

	@Override
	public Optional<StateTransformer<ToPixelized<T>, CfgNRGPixelized>> secondaryReport() {
		return Optional.of(
			createCompose()
		);
	}
	
	private Compose<ToPixelized<T>,CfgNRGPixelized,T> createCompose() {
		Compose<ToPixelized<T>,CfgNRGPixelized,T> compose = new Compose<>();
		compose.setFirst( new RetrieveSourceFromPixelized<T>() );
		compose.setSecond( secondary );
		return compose;
	}

	public StateTransformerBean<T, CfgNRGPixelized> getSecondary() {
		return secondary;
	}

	public void setSecondary(StateTransformerBean<T, CfgNRGPixelized> secondary) {
		this.secondary = secondary;
	}

}
