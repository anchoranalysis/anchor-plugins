package org.anchoranalysis.plugin.mpp.feature.bean.memo.ind;

import org.anchoranalysis.anchor.mpp.feature.bean.nrg.elem.FeatureSingleMemo;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputSingleMemo;

/*
 * #%L
 * anchor-plugin-mpp-feature
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
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.ChildCacheName;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.histogram.FeatureInputHistogram;
import org.anchoranalysis.image.feature.histogram.Mean;

import ch.ethz.biol.cell.mpp.mark.pixelstatisticsfrommark.MarkRegion;

public class Intensity extends FeatureSingleMemo {

	// START BEAN
	/** Feature to apply to the histogram */
	@BeanField
	private Feature<FeatureInputHistogram> item = new Mean();
	
	/** If TRUE, zeros are excluded from considering in the histogram */
	@BeanField
	private boolean excludeZero = false;
	
	@BeanField
	private MarkRegion region;
	// END BEAN
			
	@Override
	public double calc( SessionInput<FeatureInputSingleMemo> input ) throws FeatureCalcException {

		return input.forChild().calc(
			item,
			new CalculateHistogramInputFromMemo(region, excludeZero),
			cacheName()
		);
	}
	
	private ChildCacheName cacheName() {
		return new ChildCacheName(
			Intensity.class,
			region.uniqueName()
		);
	}

	@Override
	public String getParamDscr() {
		return region.getBeanDscr();
	}

	public boolean isExcludeZero() {
		return excludeZero;
	}

	public void setExcludeZero(boolean excludeZero) {
		this.excludeZero = excludeZero;
	}

	public MarkRegion getRegion() {
		return region;
	}

	public void setRegion(MarkRegion region) {
		this.region = region;
	}

	public Feature<FeatureInputHistogram> getItem() {
		return item;
	}

	public void setItem(Feature<FeatureInputHistogram> item) {
		this.item = item;
	}
}
