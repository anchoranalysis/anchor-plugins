package org.anchoranalysis.plugin.image.feature.histogram.calculation;

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

import org.anchoranalysis.feature.cache.calculation.CalculationResolver;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.image.feature.histogram.FeatureInputHistogram;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.plugin.image.feature.object.calculation.delegate.CalculateInputFromDelegate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class CalculateHistogramInputFromHistogram extends CalculateInputFromDelegate<FeatureInputHistogram, FeatureInputHistogram, Histogram> {
	
	public CalculateHistogramInputFromHistogram(
			ResolvedCalculation<Histogram, FeatureInputHistogram> ccDelegate) {
		super(ccDelegate);
	}
	
	public CalculateHistogramInputFromHistogram(FeatureCalculation<Histogram, FeatureInputHistogram> ccDelegate,
			CalculationResolver<FeatureInputHistogram> cache) {
		super(ccDelegate, cache);
	}

	@Override
	protected FeatureInputHistogram deriveFromDelegate(FeatureInputHistogram params, Histogram delegate) {
		return new FeatureInputHistogram(
			delegate,
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
		CalculateHistogramInputFromHistogram rhs = (CalculateHistogramInputFromHistogram) obj;
		return new EqualsBuilder()
             .append(getDelegate(), rhs.getDelegate())
             .isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(getDelegate())
			.toHashCode();
	}
}
