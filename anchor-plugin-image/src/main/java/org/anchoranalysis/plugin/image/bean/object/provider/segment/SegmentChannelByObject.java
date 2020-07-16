/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider.segment;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.bean.segment.object.SegmentChannelIntoObjects;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.object.provider.ObjectCollectionProviderUnaryWithChannel;

/**
 * Performs segmentation of a channel using each object in the upstream collection as a mask.
 *
 * <p>Specifically, a segmentation is performed for each object-mask in the upstream collection, and
 * then the results are combined.
 *
 * <p>This is useful when a partition (segmentation) of a channel already exists, and a futher
 * segmentation is desired without breaking the boundaries of the initial partition (segmentation).
 *
 * @author Owen Feehan
 */
public class SegmentChannelByObject extends ObjectCollectionProviderUnaryWithChannel {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private SegmentChannelIntoObjects segment;
    // END BEAN PROPERTIES

    @Override
    public ObjectCollection createFromObjects(
            ObjectCollection objectsSource, Channel channelToSegment) throws CreateException {
        try {
            return objectsSource.stream()
                    .flatMapWithException(
                            SegmentationFailedException.class,
                            object -> segmentObject(object, channelToSegment));

        } catch (SegmentationFailedException e) {
            throw new CreateException(e);
        }
    }

    private ObjectCollection segmentObject(ObjectMask object, Channel channelToSegment)
            throws SegmentationFailedException {
        return segment.segment(channelToSegment, Optional.of(object), Optional.empty());
    }
}
