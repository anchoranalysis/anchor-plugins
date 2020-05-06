package org.anchoranalysis.plugin.operator.feature.bean.range;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.input.FeatureInput;

/** 
 * A base-class for setting the boundaries of the range using constant scalars
 **/
public abstract class RangeCompareFromScalars<T extends FeatureInput> extends RangeCompareBase<T>  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	/** Boundary value indicating minimally-allowed range value */
	@BeanField
	private double min = Double.NEGATIVE_INFINITY;

	/** Boundary value indicating maximally-allowed range value */
	@BeanField
	private double max = Double.POSITIVE_INFINITY;
	// END BEAN PROPERTIES

	@Override
	protected double boundaryMin(SessionInput<T> input) {
		return min;
	}

	@Override
	protected double boundaryMax(SessionInput<T> input) {
		return max;
	}	
	
	@Override
	public String getParamDscr() {
		return String.format("min=%f,max=%f,%s", min, max, super.getParamDscr() );
	}
	
	public double getMin() {
		return min;
	}

	public void setMin(double min) {
		this.min = min;
	}

	public double getMax() {
		return max;
	}

	public void setMax(double max) {
		this.max = max;
	}
}
