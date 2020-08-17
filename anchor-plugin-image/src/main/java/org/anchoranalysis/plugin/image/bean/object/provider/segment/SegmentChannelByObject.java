/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

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
                    .flatMap(
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
