/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.shared.intersecting;

import org.anchoranalysis.bean.annotation.BeanField;

public abstract class FeatureIntersectingObjectsThreshold
        extends FeatureIntersectingObjectsSingleElement {

    // START BEAN PROPERTIES
    /** Only considers values greater or equal to the threshold */
    @BeanField private double threshold = 0.0;
    // END BEAN PROPERTIES

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }
}
