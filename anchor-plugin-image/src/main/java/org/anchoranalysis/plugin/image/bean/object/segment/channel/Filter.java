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

package org.anchoranalysis.plugin.image.bean.object.segment.channel;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.bean.nonbean.segment.SegmentationFailedException;
import org.anchoranalysis.image.bean.object.ObjectFilter;
import org.anchoranalysis.image.bean.segment.object.SegmentChannelIntoObjects;
import org.anchoranalysis.image.bean.segment.object.SegmentChannelIntoObjectsUnary;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectMask;

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
            Optional<ObjectCollection> seeds,
            SegmentChannelIntoObjects upstreamSegmenter)
            throws SegmentationFailedException {
        return filterObjects(
                upstreamSegmenter.segment(channel, object, seeds), channel.dimensions());
    }

    private ObjectCollection filterObjects(ObjectCollection objects, Dimensions dim)
            throws SegmentationFailedException {
        try {
            return filter.filter(objects, Optional.of(dim));
        } catch (OperationFailedException e) {
            throw new SegmentationFailedException(e);
        }
    }
}
