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
package org.anchoranalysis.plugin.image.bean.object.segment.reduce;

import java.util.Iterator;
import java.util.List;
import java.util.function.DoubleToIntFunction;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.bean.threshold.ThresholderGlobal;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.channel.factory.ChannelFactory;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.mask.Mask;
import org.anchoranalysis.image.inference.segment.WithConfidence;
import org.anchoranalysis.image.voxel.datatype.UnsignedByteVoxelType;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.histogram.threshold.Constant;
import org.anchoranalysis.spatial.box.BoundingBox;

/**
 * Merges individual objects (with confidence) together if spatially adjacent.
 *
 * <p>The confidence of each voxel in an object is projected onto a channel (always taking the max
 * if the voxel belongs to multiple objects).
 *
 * <p>This channel is then thresholded, and split into connected components, with a confidence value
 * inferred for the new objects.
 *
 * <p>The confidence value is the mean of the confidence of each individual voxel in the mask.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class DeriveObjectsFromStream {

    /**
     * Merges individual objects (with confidence) together if spatially adjacent.
     *
     * <p>This is achieved via projecting the maximum-confidence of each value into a raster, as per
     * class Javadoc.
     *
     * @param elements a stream of elements which form the input, and which may be merged.
     * @param containingBox a bounding-box that should contain all {@code elements}.
     * @param minConfidence only final (after merging) objects with at least this average confidence
     *     are outputted.
     * @param minNumberVoxels only final (after merging) objects with at least these number of
     *     voxels are outputted.
     * @return a list of object-masks, after possibly, merging, as per the above.
     * @throws OperationFailedException if the operation cannot succeed.
     */
    public static List<WithConfidence<ObjectMask>> deriveObjects(
            Stream<WithConfidence<ObjectMask>> elements,
            BoundingBox containingBox,
            double minConfidence,
            int minNumberVoxels)
            throws OperationFailedException {
        ConfidenceScaler<ObjectMask> scaler = new ConfidenceScaler<>(minConfidence, 1.0);

        Channel channel = writeConfidenceIntoChannel(elements, containingBox, scaler::downscale);

        Mask mask = threshold(channel.duplicate(), minConfidence, scaler::downscale);
        return DeriveObjectsFromMask.splitIntoObjects(
                mask, channel, scaler::upscale, containingBox.cornerMin(), minNumberVoxels);
    }

    private static Mask threshold(
            Channel channel, double minConfidence, DoubleToIntFunction convertConfidence)
            throws OperationFailedException {
        // If we pass a lower minConfidence value than appears in any element, it will
        //  be scaled to a negative value, so we must adjust to bring it to 1, otherwise non-objects
        // will be included.
        int minConfidenceNormalized = Math.max(convertConfidence.applyAsInt(minConfidence), 1);
        ThresholderGlobal thresholder =
                new ThresholderGlobal(new Constant(minConfidenceNormalized));

        return new Mask(thresholder.threshold(channel.voxels()));
    }

    private static Channel writeConfidenceIntoChannel(
            Stream<WithConfidence<ObjectMask>> elements,
            BoundingBox boxOverall,
            DoubleToIntFunction convertConfidence) {
        Dimensions dimensions = new Dimensions(boxOverall.extent());
        Channel channel =
                ChannelFactory.instance()
                        .get(UnsignedByteVoxelType.INSTANCE)
                        .createEmptyInitialised(dimensions);

        for (Iterator<WithConfidence<ObjectMask>> iterator = elements.iterator();
                iterator.hasNext(); ) {

            WithConfidence<ObjectMask> element = iterator.next();

            int confidenceAsInt = convertConfidence.applyAsInt(element.getConfidence());

            // Assign a value to the voxels only if it is greater than the existing-value
            channel.assignValue(confidenceAsInt)
                    .toObjectIf(
                            element.getElement().relativeMaskTo(boxOverall),
                            voxelValue -> voxelValue == 0 || voxelValue > confidenceAsInt);
        }

        return channel;
    }
}
