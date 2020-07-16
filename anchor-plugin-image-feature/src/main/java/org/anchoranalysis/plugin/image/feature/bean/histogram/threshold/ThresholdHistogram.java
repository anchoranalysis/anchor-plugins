/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.histogram.threshold;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.ChildCacheName;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.bean.threshold.CalculateLevel;
import org.anchoranalysis.image.feature.bean.FeatureHistogram;
import org.anchoranalysis.image.feature.histogram.FeatureInputHistogram;

/**
 * Thresholds the histogram (using a weightedOtsu) and then applies a feature to the thresholded
 * version
 *
 * @author feehano
 */
public class ThresholdHistogram extends FeatureHistogram {

    // START BEAN PROPERTIES
    @BeanField private Feature<FeatureInputHistogram> item;

    // START BEAN PROPERTIES
    @BeanField private CalculateLevel calculateLevel;
    // END BEAN PROPERTIES

    @Override
    public double calc(SessionInput<FeatureInputHistogram> input) throws FeatureCalcException {

        return input.forChild()
                .calc(
                        item,
                        new CalculateHistogramInput(
                                new CalculateOtsuThresholdedHistogram(calculateLevel, getLogger()),
                                input.resolver()),
                        new ChildCacheName(ThresholdHistogram.class, calculateLevel.hashCode()));
    }

    public Feature<FeatureInputHistogram> getItem() {
        return item;
    }

    public void setItem(Feature<FeatureInputHistogram> item) {
        this.item = item;
    }

    public CalculateLevel getCalculateLevel() {
        return calculateLevel;
    }

    public void setCalculateLevel(CalculateLevel calculateLevel) {
        this.calculateLevel = calculateLevel;
    }
}
