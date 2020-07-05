package org.anchoranalysis.plugin.image.feature.bean.pixelscore;

import java.util.List;
import java.util.Optional;

/*
 * #%L
 * anchor-plugin-image-feature
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


import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.image.histogram.Histogram;

/**
 * This assumes the histograms correspond to the nrg channels exactly (in terms of indexing)
 * 
 * @author Owen Feehan
 *
 */
public class PixelScoreNormalizeByMax extends PixelScoreSingleChnl {

	private double maxEdge;

	@Override
	public void init(List<Histogram> histograms, Optional<KeyValueParams> keyValueParams) throws InitException {
		try {
			maxEdge = histograms.get( getNrgChnlIndex() ).calcMax();
		} catch (OperationFailedException e) {
			throw new InitException(e);
		}
	}

	@Override
	protected double deriveScoreFromPixelVal(int pixelVal) {
		return pixelVal/maxEdge;
	}
}
