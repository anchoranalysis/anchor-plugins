package ch.ethz.biol.cell.mpp.nrg.feature.histogram;

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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.CacheableParams;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.params.FeatureCalcParams;
import org.anchoranalysis.feature.calc.params.FeatureCalcParamsDescriptor;
import org.anchoranalysis.image.feature.bean.FeatureHistogram;
import org.anchoranalysis.image.feature.histogram.FeatureHistogramParams;

public class ConvertToRatioOfHistogramTotalCount extends FeatureHistogram {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private Feature<FeatureHistogramParams> item;
	// END BEAN PROPERTIES
	
	@Override
	public double calcCast(CacheableParams<FeatureHistogramParams> params)
			throws FeatureCalcException {

		double val = params.calc(item);
		return val/ params.getParams().getHistogram().getTotalCount();
	}
	
	@Override
	public String getDscrLong() {
		return String.format("convert_ratio(%s)", getItem().getDscrLong() );
	}

	public Feature<FeatureHistogramParams> getItem() {
		return item;
	}

	public void setItem(Feature<FeatureHistogramParams> item) {
		this.item = item;
	}

	@Override
	public FeatureCalcParamsDescriptor paramType()
			throws FeatureCalcException {
		return FeatureCalcParamsDescriptor.instance;
	}
}
