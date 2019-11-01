package ch.ethz.biol.cell.mpp.mark.provider;

import org.anchoranalysis.anchor.mpp.mark.Mark;

/*
 * #%L
 * anchor-plugin-mpp
 * %%
 * Copyright (C) 2016 ETH Zurich, University of Zurich, Owen Feehan
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
import org.anchoranalysis.core.error.CreateException;

import ch.ethz.biol.cell.mpp.cfg.Cfg;
import ch.ethz.biol.cell.mpp.cfg.provider.CfgProvider;

// Retrieves a mark from a cfg, assuming there is only one mark in a cfg, otherwise throwing an error
public class RetrieveSingletonMark extends MarkProvider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private CfgProvider cfgProvider;
	// END BEAN PROPERTIES

	@Override
	public Mark create() throws CreateException {
		Cfg cfg = cfgProvider.create();
		
		if (cfg.size()==0) {
			throw new CreateException("Cfg is empty. It must have exactly one item");
		}
		
		if (cfg.size()>1) {
			throw new CreateException("Cfg has multiple marks. It must have exactly one item");
		}
		
		return cfg.get(0);
	}

	public CfgProvider getCfgProvider() {
		return cfgProvider;
	}

	public void setCfgProvider(CfgProvider cfgProvider) {
		this.cfgProvider = cfgProvider;
	}


}
