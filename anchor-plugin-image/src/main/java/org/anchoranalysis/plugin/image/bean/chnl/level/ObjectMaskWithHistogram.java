/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.chnl.level;

import lombok.Getter;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramFactory;
import org.anchoranalysis.image.object.ObjectMask;

class ObjectMaskWithHistogram {

    @Getter private final ObjectMask object;

    @Getter private final Histogram histogram;

    public ObjectMaskWithHistogram(ObjectMask object, Channel chnl) {
        this.object = object;
        this.histogram = HistogramFactory.create(chnl, object);
    }
}
