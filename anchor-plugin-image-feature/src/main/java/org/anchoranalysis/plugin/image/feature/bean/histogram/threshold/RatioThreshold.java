package org.anchoranalysis.plugin.image.feature.bean.histogram.threshold;



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
import org.anchoranalysis.bean.shared.relation.threshold.RelationToThreshold;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.histogram.FeatureHistogramStatistic;
import org.anchoranalysis.image.histogram.Histogram;

public class RatioThreshold extends FeatureHistogramStatistic {

	// START BEAN PROPERTIES
	@BeanField
	private RelationToThreshold threshold;
	// END BEAN PROPERTIES
	
	@Override
	protected double calcStatisticFrom(Histogram histogram) throws FeatureCalcException {
		if (histogram.size()==0) {
			return 0.0;
		}
		
		assert(histogram.size() > 0);
		
		long count = histogram.countThreshold(threshold);
		return (((double) count) / histogram.size());
	}
	
	@Override
	public String getParamDscr() {
		return String.format(
			"%s,threshold=%s",
			super.getParamDscr(),
			threshold.toString()
		);
	}

	public RelationToThreshold getThreshold() {
		return threshold;
	}

	public void setThreshold(RelationToThreshold threshold) {
		this.threshold = threshold;
	}
}