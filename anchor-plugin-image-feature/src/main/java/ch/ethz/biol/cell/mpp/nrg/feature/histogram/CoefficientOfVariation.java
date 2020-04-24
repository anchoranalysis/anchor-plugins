package ch.ethz.biol.cell.mpp.nrg.feature.histogram;

import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.FeatureHistogram;
import org.anchoranalysis.image.feature.histogram.FeatureInputHistogram;
import org.anchoranalysis.image.histogram.Histogram;

public class CoefficientOfVariation extends FeatureHistogram {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected double calc(SessionInput<FeatureInputHistogram> input) throws FeatureCalcException {
		Histogram hist = input.get().getHistogram();

		double mean = hist.mean();
		
		if (mean==0) {
			return Double.NaN;
		}
		
		return hist.stdDev() / mean;
	}

}
