/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.intensity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.feature.histogram.FeatureInputHistogram;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramFactory;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
class CalculateHistogramForNrgChannel
        extends FeatureCalculation<FeatureInputHistogram, FeatureInputSingleObject> {

    /**
     * iff TRUE zero-intensity values are excluded from the histogram, otherwise they are included
     */
    private boolean excludeZero = false;

    /** an index uniquely identifying the channel */
    private int nrgIndex;

    /** the channel corresponding to nrgIndex */
    @EqualsAndHashCode.Exclude private Channel channel;

    @Override
    protected FeatureInputHistogram execute(FeatureInputSingleObject params) {

        Histogram hist =
                HistogramFactory.createHistogramIgnoreZero(
                        channel, params.getObject(), excludeZero);

        return new FeatureInputHistogram(hist, params.getResOptional());
    }
}
