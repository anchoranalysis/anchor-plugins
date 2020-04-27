package org.anchoranalysis.plugin.image.feature.bean.obj.single.intensity;

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
