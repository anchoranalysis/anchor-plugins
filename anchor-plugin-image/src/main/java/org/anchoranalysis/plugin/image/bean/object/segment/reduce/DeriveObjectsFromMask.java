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

import com.google.common.base.Preconditions;
import java.util.List;
import java.util.function.DoubleUnaryOperator;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.mask.Mask;
import org.anchoranalysis.image.core.object.HistogramFromObjectsFactory;
import org.anchoranalysis.image.voxel.binary.connected.ObjectsFromConnectedComponentsFactory;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.math.histogram.Histogram;
import org.anchoranalysis.plugin.image.segment.WithConfidence;
import org.anchoranalysis.spatial.point.ReadableTuple3i;

/**
 * Derives individual objects (with confidence) from a mask and associated channel of confidence
 * values.
 *
 * <p>The confidence value is the mean of the confidence of each individual voxel in the mask.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class DeriveObjectsFromMask {

    /**
     * Splits a mask into connected-components and associates a confidence.
     *
     * @param mask mask to split into connected-components
     * @param channel a channel, the same size as {@code mask} with a confidence-value for each
     *     voxel in a {@code mask}.
     * @param transformToConfidence transforms from the unsigned-integer found in {@code channel} to
     *     a confidence value {@code 0 <= confidence <= 1}.
     * @param shift a shift to add to the object-masks after extracting the confidence-level.
     * @param minNumberVoxels the minimum number of voxels that must exist to form a separate
     *     object, otherwise the voxels are ignored.
     * @return a list of objects created from the connected-components of the mask with associated
     *     confidence-values
     */
    public static List<WithConfidence<ObjectMask>> splitIntoObjects(
            Mask mask,
            Channel channel,
            DoubleUnaryOperator transformToConfidence,
            ReadableTuple3i shift,
            int minNumberVoxels)
            throws OperationFailedException {
        Preconditions.checkArgument(mask.extent().equals(channel.extent()));

        ObjectsFromConnectedComponentsFactory creator =
                new ObjectsFromConnectedComponentsFactory(minNumberVoxels);

        // All the objects
        ObjectCollection objects = creator.createUnsignedByte(mask.binaryVoxels());

        // Associate a confidence value, by the mean-intensity of all confidence voxels in the mask
        return objects.stream()
                .mapToList(
                        object ->
                                deriveConfidenceAndShift(
                                        object, channel, transformToConfidence, shift));
    }

    private static WithConfidence<ObjectMask> deriveConfidenceAndShift(
            ObjectMask object,
            Channel channel,
            DoubleUnaryOperator transformToConfidence,
            ReadableTuple3i shift)
            throws OperationFailedException {
        return new WithConfidence<>(
                object.shiftBy(shift), confidenceForObject(object, channel, transformToConfidence));
    }

    /**
     * The mean value of all the confidence values for each voxel in the channel, translated-back
     */
    private static double confidenceForObject(
            ObjectMask object, Channel channel, DoubleUnaryOperator unscale)
            throws OperationFailedException {
        Histogram histogram = HistogramFromObjectsFactory.createFrom(channel, object);
        return unscale.applyAsDouble(histogram.mean());
    }
}
