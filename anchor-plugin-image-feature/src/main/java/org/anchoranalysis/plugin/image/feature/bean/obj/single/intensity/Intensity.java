package org.anchoranalysis.plugin.image.feature.bean.obj.single.intensity;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.ChildCacheName;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.feature.histogram.FeatureInputHistogram;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;

import ch.ethz.biol.cell.mpp.nrg.feature.histogram.Mean;

/**
 * Derives a histogram for an object against one channel from the NRG-stack, and applies a {@link org.anchoranalysis.image.feature.bean.FeatureHistogram} to it.
 * 
 * @author Owen Feehan
 *
 */
public class Intensity extends FeatureNrgChnl {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	/** Feature to apply to the histogram */
	@BeanField
	private Feature<FeatureInputHistogram> item = new Mean();
	
	/** Iff TRUE, zero-valued voxels are excluded from the histogrma */
	@BeanField
	private boolean excludeZero = false;
	// END BEAN PROEPRTIES
	
	@Override
	protected double calcForChnl(SessionInput<FeatureInputSingleObj> input, Chnl chnl) throws FeatureCalcException {
		return input.calcChild(
			item,
			new CalculateHistogramForNrgChnl(excludeZero, getNrgIndex(), chnl),
			cacheName()
		);
	}
	
	private ChildCacheName cacheName() {
		return new ChildCacheName(
			Intensity.class,
			String.valueOf(excludeZero) + "_" + String.valueOf(getNrgIndex())
		);
	}

	public Feature<FeatureInputHistogram> getItem() {
		return item;
	}

	public void setItem(Feature<FeatureInputHistogram> item) {
		this.item = item;
	}

	public boolean isExcludeZero() {
		return excludeZero;
	}

	public void setExcludeZero(boolean excludeZero) {
		this.excludeZero = excludeZero;
	}
}
