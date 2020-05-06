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

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.ChildCacheName;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.feature.histogram.FeatureInputHistogram;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;

import ch.ethz.biol.cell.mpp.nrg.feature.histogram.Mean;

/**
 * Derives a histogram for an object against one channel from the NRG-stack, and applies a {@link org.anchoranalysis.image.feature.bean.FeatureHistogram} to it.
 * 
 * @author Owen Feehan
 *
 */
public class Intensity extends FeatureNrgChnl {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	/** Feature to apply to the histogram */
	@BeanField
	private Feature<FeatureInputHistogram> item = new Mean();
	
	/** Iff TRUE, zero-valued voxels are excluded from the histogrma */
	@BeanField
	private boolean excludeZero = false;
	// END BEAN PROEPRTIES
	
	@Override
	protected double calcForChnl(SessionInput<FeatureInputSingleObj> input, Chnl chnl) throws FeatureCalcException {
		return input.forChild().calc(
			item,
			new CalculateHistogramForNrgChnl(excludeZero, getNrgIndex(), chnl),
			cacheName()
		);
	}
	
	private ChildCacheName cacheName() {
		return new ChildCacheName(
			Intensity.class,
			String.valueOf(excludeZero) + "_" + String.valueOf(getNrgIndex())
		);
	}

	public Feature<FeatureInputHistogram> getItem() {
		return item;
	}

	public void setItem(Feature<FeatureInputHistogram> item) {
		this.item = item;
	}

	public boolean isExcludeZero() {
		return excludeZero;
	}

	public void setExcludeZero(boolean excludeZero) {
		this.excludeZero = excludeZero;
	}
}
