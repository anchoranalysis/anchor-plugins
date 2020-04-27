package ch.ethz.biol.cell.mpp.nrg.feature.histogram;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.histogram.Histogram;

public class Median extends FeatureHistogramStatistic {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected double calcStatisticFrom(Histogram histogram) throws OperationFailedException {
		return histogram.quantile(0.5);
	}
}
