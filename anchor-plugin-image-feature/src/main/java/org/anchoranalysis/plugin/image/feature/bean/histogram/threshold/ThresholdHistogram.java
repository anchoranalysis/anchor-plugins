package org.anchoranalysis.plugin.image.feature.bean.histogram.threshold;

/*-
 * #%L
 * anchor-plugin-image-feature
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
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.ChildCacheName;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.bean.threshold.CalculateLevel;
import org.anchoranalysis.image.feature.bean.FeatureHistogram;
import org.anchoranalysis.image.feature.histogram.FeatureInputHistogram;
import org.anchoranalysis.plugin.image.feature.histogram.calculation.CalculateHistogramInputFromHistogram;
import org.anchoranalysis.plugin.image.feature.histogram.calculation.CalculateOtsuThresholdedHistogram;

/**
 * Thresholds the histogram (using a weightedOtsu) and then applies
 *  a feature to the thresholded version
 *  
 * @author feehano
 *
 */
public class ThresholdHistogram extends FeatureHistogram {

	// START BEAN PROPERTIES
	@BeanField
	private Feature<FeatureInputHistogram> item;
	
	// START BEAN PROPERTIES
	@BeanField
	private CalculateLevel calculateLevel;
	// END BEAN PROPERTIES
	
	@Override
	public double calc(SessionInput<FeatureInputHistogram> input) throws FeatureCalcException {
		
		return input.forChild().calc(
			item,
			new CalculateHistogramInputFromHistogram(
				new CalculateOtsuThresholdedHistogram(calculateLevel, getLogger()),
				input.resolver()
			),
			new ChildCacheName(ThresholdHistogram.class, calculateLevel.hashCode())
		);
	}

	public Feature<FeatureInputHistogram> getItem() {
		return item;
	}

	public void setItem(Feature<FeatureInputHistogram> item) {
		this.item = item;
	}

	public CalculateLevel getCalculateLevel() {
		return calculateLevel;
	}

	public void setCalculateLevel(CalculateLevel calculateLevel) {
		this.calculateLevel = calculateLevel;
	}
}
