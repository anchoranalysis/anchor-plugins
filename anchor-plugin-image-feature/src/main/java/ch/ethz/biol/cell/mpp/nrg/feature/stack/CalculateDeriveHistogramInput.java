package ch.ethz.biol.cell.mpp.nrg.feature.stack;

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
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.histogram.FeatureInputHistogram;
import org.anchoranalysis.image.feature.stack.FeatureInputStack;
import org.anchoranalysis.image.histogram.Histogram;
import org.apache.commons.lang.builder.EqualsBuilder;

public class CalculateDeriveHistogramInput extends FeatureCalculation<FeatureInputHistogram, FeatureInputStack> {

	private ResolvedCalculation<Histogram, FeatureInputStack> histogramCalculation;

	public CalculateDeriveHistogramInput(
		FeatureCalculation<Histogram, FeatureInputStack> histogramCalculation,
		CalculationResolver<FeatureInputStack> resolver
	) {
		this(
			resolver.search(histogramCalculation)
		);
	}
	
	public CalculateDeriveHistogramInput(ResolvedCalculation<Histogram, FeatureInputStack> histogramCalculation) {
		super();
		this.histogramCalculation = histogramCalculation;
	}

	@Override
	protected FeatureInputHistogram execute(FeatureInputStack input) throws FeatureCalcException {
		return new FeatureInputHistogram(
			histogramCalculation.getOrCalculate(input),
			input.getResOptional()
		);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) { return false; }
		if (obj == this) { return true; }
		if (obj.getClass() != getClass()) {
			return false;
		}
		CalculateDeriveHistogramInput rhs = (CalculateDeriveHistogramInput) obj;
		return new EqualsBuilder()
             .append(histogramCalculation, rhs.histogramCalculation)
             .isEquals();
	}

	@Override
	public int hashCode() {
		return histogramCalculation.hashCode();
	}
}
