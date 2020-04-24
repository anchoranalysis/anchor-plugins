package ch.ethz.biol.cell.mpp.nrg.feature.histogram;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.FeatureHistogram;
import org.anchoranalysis.image.feature.histogram.FeatureInputHistogram;
import org.anchoranalysis.image.histogram.Histogram;

/**
 * The range (difference in values) between two quantiles
 * 
 * @author Owen Feehan
 *
 */
public class Range extends FeatureHistogram {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private double quantileLow = 0;
	
	@BeanField
	private double quantileHigh = 1.0;
	// END BEAN PROPERTIES

	@Override
	protected double calc(SessionInput<FeatureInputHistogram> input) throws FeatureCalcException {

		Histogram hist = input.get().getHistogram();
		
		double high = hist.quantile(quantileHigh);
		double low = hist.quantile(quantileLow);
		return high-low;
	}
	
	public double getQuantileLow() {
		return quantileLow;
	}

	public void setQuantileLow(double quantileLow) {
		this.quantileLow = quantileLow;
	}

	public double getQuantileHigh() {
		return quantileHigh;
	}

	public void setQuantileHigh(double quantileHigh) {
		this.quantileHigh = quantileHigh;
	}

}
