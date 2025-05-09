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
import org.anchoranalysis.image.bean.segment.object.SegmentChannelIntoObjectsUnary;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.VoxelsUntyped;
import org.anchoranalysis.image.voxel.binary.BinaryVoxels;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectMask;

/**
 * Perform a segmentation in a MIP instead of z-stacks, and fits the result back into a 3D
 * segmentation.
 *
 * <p>The upstream segmentation should return 2D objects as it is executed on the maximum-intensity
 * projection.
 *
 * <p>A 3D binary-segmentation is applied to the z-stack with {@code segmentStack} to produce a mask
 * over the z-stack. The the 2D objects are then expanded in the z-dimension to fit this mask.
 *
 * @author Owen Feehan
 */
public class SegmentOnMaximumIntensityAndExpandInZ extends SegmentChannelIntoObjectsUnary {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private BinarySegmentation segmentStack;

    // END BEAN PROPERTIES

    @Override
    public ObjectCollection segment(
            Channel channel,
            Optional<ObjectMask> objectMask,
            Optional<ObjectCollection> seeds,
            SegmentChannelIntoObjects upstreamSegmenter)
            throws SegmentationFailedException {

        if (objectMask.isPresent()) {
            throw new SegmentationFailedException(
                    "An object-mask is not supported for this operation");
        }

        // Collapse seeds in z direction
        seeds.ifPresent(SegmentOnMaximumIntensityAndExpandInZ::flattenSeedsInZ);

        ObjectCollection objects =
                upstreamSegmenter.segment(channel.projectMax(), Optional.empty(), seeds);

        if (isAny3d(objects)) {
            throw new SegmentationFailedException(
                    "A 3D object was returned from the initial segmentation. This must return only 2D objects");
        }

        return ExtendInZHelper.extendObjects(objects, binarySgmn(channel));
    }

    private boolean isAny3d(ObjectCollection objects) {
        return objects.stream().anyMatch(objectMask -> objectMask.extent().z() > 1);
    }

    private BinaryVoxels<UnsignedByteBuffer> binarySgmn(Channel channel)
            throws SegmentationFailedException {
        BinarySegmentationParameters parameters =
                new BinarySegmentationParameters(channel.resolution());

        Voxels<UnsignedByteBuffer> voxels = channel.voxels().asByte();

        Voxels<UnsignedByteBuffer> stackBinary = voxels.duplicate();
        return segmentStack.segment(new VoxelsUntyped(stackBinary), parameters, Optional.empty());
    }

    private static ObjectCollection flattenSeedsInZ(ObjectCollection seeds) {
        return seeds.stream().map(ObjectMask::flattenZ);
    }
}
