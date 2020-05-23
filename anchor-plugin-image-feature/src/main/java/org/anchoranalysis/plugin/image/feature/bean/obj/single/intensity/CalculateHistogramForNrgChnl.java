package org.anchoranalysis.plugin.image.feature.bean.obj.single.intensity;

/*-
 * #%L
 * anchor-plugin-image-feature
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan
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

import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.feature.histogram.FeatureInputHistogram;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramFactory;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

class CalculateHistogramForNrgChnl extends FeatureCalculation<FeatureInputHistogram, FeatureInputSingleObj> {

	private boolean excludeZero = false;
	private int nrgIndex;
	private Chnl chnl;

	/**
	 * Constructor
	 * 
	 * @param excludeZero iff TRUE zero-intensity values are excluded from the histogram, otherwise they are included
	 * @param nrgIndex an index uniquely identifying the channel
	 * @param chnl the channel corresponding to nrgIndex
	 */
	public CalculateHistogramForNrgChnl(boolean excludeZero, int nrgIndex, Chnl chnl) {
		super();
		this.excludeZero = excludeZero;
		this.nrgIndex = nrgIndex;
		this.chnl = chnl;
	}
	
	@Override
	protected FeatureInputHistogram execute(FeatureInputSingleObj params) {

		Histogram hist = HistogramFactory.createHistogramIgnoreZero(
			chnl,
			params.getObjMask(),
			excludeZero
		);
		
		return new FeatureInputHistogram(
			hist,
			params.getResOptional()
		);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) { return false; }
		if (obj == this) { return true; }
		if (obj.getClass() != getClass()) {
			return false;
		}
		CalculateHistogramForNrgChnl rhs = (CalculateHistogramForNrgChnl) obj;
		return new EqualsBuilder()
			.append(excludeZero, rhs.excludeZero)
			.append(nrgIndex, rhs.nrgIndex)
            .isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(excludeZero)
			.append(nrgIndex)
			.toHashCode();
	}
}
