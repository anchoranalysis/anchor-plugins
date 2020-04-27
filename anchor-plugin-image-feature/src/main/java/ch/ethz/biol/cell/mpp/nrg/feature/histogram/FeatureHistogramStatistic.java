package ch.ethz.biol.cell.mpp.nrg.feature.histogram;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.FeatureHistogram;
import org.anchoranalysis.image.feature.histogram.FeatureInputHistogram;
import org.anchoranalysis.image.histogram.Histogram;

public abstract class FeatureHistogramStatistic extends FeatureHistogram {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public double calc(SessionInput<FeatureInputHistogram> input) throws FeatureCalcException {
		try {
			return calcStatisticFrom(
				input.get().getHistogram()
			);
		} catch (OperationFailedException e) {
			throw new FeatureCalcException(e);
		}
	}
	
	protected abstract double calcStatisticFrom( Histogram histogram ) throws OperationFailedException;
}
