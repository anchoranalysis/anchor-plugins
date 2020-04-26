package ch.ethz.biol.cell.mpp.nrg.feature.histogram;

import org.anchoranalysis.feature.cache.calculation.CalculationResolver;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.image.feature.histogram.FeatureInputHistogram;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.plugin.image.feature.obj.pair.CalculateInputFromDelegate;
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
