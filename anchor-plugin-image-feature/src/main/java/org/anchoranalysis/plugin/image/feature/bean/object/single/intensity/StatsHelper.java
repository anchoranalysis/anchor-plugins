/*-
 * #%L
 * anchor-plugin-image-feature
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

package org.anchoranalysis.plugin.image.feature.bean.object.single.intensity;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.object.HistogramFromObjectsFactory;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.math.histogram.Histogram;
import org.anchoranalysis.plugin.image.intensity.IntensityMeanCalculator;

/** Helper class for calculating various statistical measures on image objects. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class StatsHelper {

    /**
     * Calculates the mean-intensity of a masked-part of each slice, and returns the maximum value
     * across all slices.
     *
     * @param channel the {@link Channel} to calculate intensity from
     * @param object the {@link ObjectMask} defining the region of interest
     * @param excludeZero if true, zero-valued voxels are excluded from the calculation
     * @return a {@link ValueAndIndex} containing the maximum mean intensity and its corresponding
     *     slice index
     * @throws FeatureCalculationException if the calculation fails
     */
    public static ValueAndIndex calculateMaxSliceMean(
            Channel channel, ObjectMask object, boolean excludeZero)
            throws FeatureCalculationException {

        double max = Double.NEGATIVE_INFINITY;
        int index = -1;

        for (int z = 0; z < object.boundingBox().extent().z(); z++) {

            // We adjust the z coordinate to point to the channel
            int zTarget = z + object.boundingBox().cornerMin().z();

            ObjectMask slice = object.extractSlice(zTarget, true);

            if (slice.voxelsOn().anyExists()) {
                double mean =
                        IntensityMeanCalculator.calculateMeanIntensityObject(
                                channel, slice, excludeZero);

                if (mean > max) {
                    index = z;
                    max = mean;
                }
            }
        }

        return new ValueAndIndex(max, index);
    }

    /**
     * Calculates the mean-intensity of a certain number of (highest or lowest-intensity) pixels
     * from the masked part of a channel.
     *
     * <p>This number of pixels can either be taken from the highest or lowest part of the
     * histogram.
     *
     * @param channel the {@link Channel} to calculate intensity from
     * @param object the {@link ObjectMask} defining the region of interest
     * @param numberVoxels the number of voxels to be considered (either the highest-intensity
     *     pixels, or lowest-intensity pixels)
     * @param highest if true, the highest-intensity voxels are used in the calculation; otherwise,
     *     the lowest-intensity pixels are used
     * @return the mean intensity of the selected voxels
     * @throws OperationFailedException if the calculation fails
     */
    public static double calculateMeanNumberVoxels(
            Channel channel, ObjectMask object, int numberVoxels, boolean highest)
            throws OperationFailedException {

        Histogram histogram = HistogramFromObjectsFactory.createFrom(channel, object);

        Histogram histogramCut =
                highest
                        ? histogram.cropRemoveSmallerValues(numberVoxels)
                        : histogram.cropRemoveLargerValues(numberVoxels);

        return histogramCut.mean();
    }
}
