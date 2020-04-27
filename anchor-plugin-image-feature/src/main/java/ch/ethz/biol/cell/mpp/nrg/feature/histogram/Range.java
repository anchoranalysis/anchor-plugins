package ch.ethz.biol.cell.mpp.nrg.feature.histogram;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.histogram.Histogram;

/**
 * The range (difference in values) between two quantiles
 * 
 * @author Owen Feehan
 *
 */
public class Range extends FeatureHistogramStatistic {

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
	protected double calcStatisticFrom(Histogram histogram) throws OperationFailedException {

		double high = histogram.quantile(quantileHigh);
		double low = histogram.quantile(quantileLow);
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
