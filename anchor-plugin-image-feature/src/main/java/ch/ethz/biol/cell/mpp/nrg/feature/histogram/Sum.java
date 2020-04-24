package ch.ethz.biol.cell.mpp.nrg.feature.histogram;

import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.FeatureHistogram;
import org.anchoranalysis.image.feature.histogram.FeatureInputHistogram;

public class Sum extends FeatureHistogram {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected double calc(SessionInput<FeatureInputHistogram> input) throws FeatureCalcException {
		return input.get().getHistogram().calcSum();
	}
}
