package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.extractscoresize;

/*-
 * #%L
 * anchor-plugin-mpp-sgmn
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import org.anchoranalysis.mpp.sgmn.optscheme.ExtractScoreSize;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.optscheme.DualCfgNRGPixelized;

public class DualCfgNRGPixelizedExtracter extends ExtractScoreSize<DualCfgNRGPixelized> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private CfgNRGPixelizedExtracter helper = new CfgNRGPixelizedExtracter();;
	
	@Override
	public double extractScore(DualCfgNRGPixelized item) {
		return helper.extractScore(item.getDest());
	}

	@Override
	public int extractSize(DualCfgNRGPixelized item) {
		return helper.extractSize(item.getDest());
	}

}