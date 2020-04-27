package ch.ethz.biol.cell.mpp.nrg.feature.stack;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.FeatureStack;
import org.anchoranalysis.image.feature.histogram.FeatureInputHistogram;
import org.anchoranalysis.image.feature.stack.FeatureInputStack;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.plugin.image.feature.bean.obj.single.intensity.CalculateDeriveHistogramInput;

import ch.ethz.biol.cell.mpp.nrg.feature.histogram.Mean;
import ch.ethz.biol.cell.mpp.nrg.feature.stack.cachedcalculation.CalculateHistogram;
import ch.ethz.biol.cell.mpp.nrg.feature.stack.cachedcalculation.CalculateHistogramMasked;

/**
 * The intensity of a particular channel of the stack, by default the mean-intensity.
 * 
 * <p>Alternative statistics to the mean can be calculated via the item bean-field.</p>
 * 
 * @author Owen Feehan
 *
 */
public class Intensity extends FeatureStack {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	/** Feature to apply to the histogram */
	@BeanField
	private Feature<FeatureInputHistogram> item = new Mean();
	
	@BeanField
	/** The channel that that forms the histogram */
	private int nrgIndex = 0;
	
	/** Optionally, index of another channel that masks the histogram. -1 disables */
	@BeanField
	private int nrgIndexMask = -1;
	// END BEAN PROEPRTIES

	@Override
	protected double calc(SessionInput<FeatureInputStack> input) throws FeatureCalcException {
		return input.calcChild(
			item,
			new CalculateDeriveHistogramInput(
				histogramCalculator(),
				input.resolver()
			),
			"intensityHistogram"
		);
	}

	private FeatureCalculation<Histogram,FeatureInputStack> histogramCalculator() {
		if (nrgIndexMask!=-1) {
			return new CalculateHistogramMasked(nrgIndex, nrgIndexMask);
		} else {
			return new CalculateHistogram(nrgIndex);
		}
	}
	
	public int getNrgIndex() {
		return nrgIndex;
	}

	public void setNrgIndex(int nrgIndex) {
		this.nrgIndex = nrgIndex;
	}

	public int getNrgIndexMask() {
		return nrgIndexMask;
	}

	public void setNrgIndexMask(int nrgIndexMask) {
		this.nrgIndexMask = nrgIndexMask;
	}
	
	public Feature<FeatureInputHistogram> getItem() {
		return item;
	}

	public void setItem(Feature<FeatureInputHistogram> item) {
		this.item = item;
	}
}
