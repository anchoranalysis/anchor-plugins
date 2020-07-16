/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.segment;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.bean.object.ObjectFilter;
import org.anchoranalysis.image.bean.segment.object.SegmentChannelIntoObjects;
import org.anchoranalysis.image.bean.segment.object.SegmentChannelIntoObjectsUnary;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.seed.SeedCollection;

/**
 * Applies an object-filter to the results of an upstream segmentation.
 *
 * @author Owen Feehan
 */
public class Filter extends SegmentChannelIntoObjectsUnary {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ObjectFilter filter; // NOSONAR
    // END BEAN PROPERTIES

    @Override
    public ObjectCollection segment(
            Channel channel,
            Optional<ObjectMask> object,
            Optional<SeedCollection> seeds,
            SegmentChannelIntoObjects upstreamSegmentation)
            throws SegmentationFailedException {
        return filterObjects(
                upstreamSegmentation.segment(channel, object, seeds), channel.getDimensions());
    }

    private ObjectCollection filterObjects(ObjectCollection objects, ImageDimensions dim)
            throws SegmentationFailedException {
        try {
            return filter.filter(objects, Optional.of(dim), Optional.empty());
        } catch (OperationFailedException e) {
            throw new SegmentationFailedException(e);
        }
    }
}
