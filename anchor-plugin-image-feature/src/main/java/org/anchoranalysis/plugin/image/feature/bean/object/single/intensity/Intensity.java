/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.intensity;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.ChildCacheName;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.feature.histogram.FeatureInputHistogram;
import org.anchoranalysis.image.feature.histogram.Mean;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;

/**
 * Calculates a statistic from the intensity values covered by a single object-mask in a channel.
 *
 * <p>Specifically, a histogram of intensity-values is constructed for the region covered by the
 * object in one specific channnel in the NRG-stack (specified by <b>nrgIndex</b>).
 *
 * <p>Then a customizable {@link org.anchoranalysis.image.feature.bean.FeatureHistogram} (specified
 * by <b>item</b>) extracts a statistic from the histogram. By default, the <i>mean</i> is
 * calculated.
 *
 * @author Owen Feehan
 */
public class Intensity extends FeatureNrgChnl {

    // START BEAN PROPERTIES
    /** Feature to apply to the histogram */
    @BeanField @Getter @Setter private Feature<FeatureInputHistogram> item = new Mean();

    /** Iff TRUE, zero-valued voxels are excluded from the histogram */
    @BeanField @Getter @Setter private boolean excludeZero = false;
    // END BEAN PROEPRTIES

    @Override
    protected double calcForChnl(SessionInput<FeatureInputSingleObject> input, Channel chnl)
            throws FeatureCalcException {
        return input.forChild()
                .calc(
                        item,
                        new CalculateHistogramForNrgChannel(excludeZero, getNrgIndex(), chnl),
                        cacheName());
    }

    private ChildCacheName cacheName() {
        return new ChildCacheName(
                Intensity.class, String.valueOf(excludeZero) + "_" + getNrgIndex());
    }
}
