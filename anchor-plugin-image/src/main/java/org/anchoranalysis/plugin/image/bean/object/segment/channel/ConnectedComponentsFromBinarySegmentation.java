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
import org.anchoranalysis.image.bean.nonbean.segment.BinarySegmentationParameters;
import org.anchoranalysis.image.bean.nonbean.segment.SegmentationFailedException;
import org.anchoranalysis.image.bean.segment.binary.BinarySegmentation;
import org.anchoranalysis.image.bean.segment.object.SegmentChannelIntoObjects;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.dimensions.Resolution;
import org.anchoranalysis.image.core.mask.Mask;
import org.anchoranalysis.image.voxel.binary.BinaryVoxels;
import org.anchoranalysis.image.voxel.binary.connected.ObjectsFromConnectedComponentsFactory;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.spatial.point.ReadableTuple3i;

/**
 * Performs a binary-segmentation of the channel and converts its connected-components into objects
 *
 * @author Owen Feehan
 */
public class ConnectedComponentsFromBinarySegmentation extends SegmentChannelIntoObjects {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private BinarySegmentation segment;

    @BeanField @Getter @Setter private int minNumberVoxels = 1;

    // END BEAN PROPERTIES

    @Override
    public ObjectCollection segment(
            Channel channel, Optional<ObjectMask> objectMask, Optional<ObjectCollection> seeds)
            throws SegmentationFailedException {

        BinarySegmentationParameters parameters =
                new BinarySegmentationParameters(channel.resolution());

        BinaryVoxels<UnsignedByteBuffer> binaryValues =
                segment.segment(channel.voxels(), parameters, objectMask);
        return createFromVoxels(
                binaryValues,
                channel.resolution(),
                objectMask.map(object -> object.boundingBox().cornerMin()));
    }

    private ObjectCollection createFromVoxels(
            BinaryVoxels<UnsignedByteBuffer> binaryValues,
            Optional<Resolution> resolution,
            Optional<ReadableTuple3i> maskShiftBy) {
        Mask mask = new Mask(binaryValues, resolution);

        ObjectsFromConnectedComponentsFactory creator =
                new ObjectsFromConnectedComponentsFactory(minNumberVoxels);
        return maybeShiftObjects(creator.createUnsignedByte(mask.binaryVoxels()), maskShiftBy);
    }

    private static ObjectCollection maybeShiftObjects(
            ObjectCollection objects, Optional<ReadableTuple3i> shiftByQuantity) {
        return shiftByQuantity.map(objects::shiftBy).orElse(objects);
    }
}
