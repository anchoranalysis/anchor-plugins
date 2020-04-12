package org.anchoranalysis.plugin.image.feature.bean.obj.intersecting;

import org.anchoranalysis.bean.annotation.BeanField;

public abstract class FeatureIntersectingObjsThreshold extends FeatureIntersectingObjsSingleElem {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	/**
	 * Only considers values greater or equal to the threshold
	 */
	@BeanField
	private double threshold = 0.0;
	// END BEAN PROPERTIES
	
	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}
}
