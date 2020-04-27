package ch.ethz.biol.cell.mpp.nrg.feature.histogram;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramStatistics;

public class CoefficientOfVariation extends FeatureHistogramStatistic {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected double calcStatisticFrom(Histogram histogram) throws OperationFailedException {
		return HistogramStatistics.coefficientOfVariation(histogram);
	}

}
