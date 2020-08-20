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

package org.anchoranalysis.plugin.image.intensity;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.arithmetic.RunningSum;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.iterator.IterateVoxelsVoxelBoxAsInt;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IntensityMeanCalculator {

    public static double calculateMeanIntensityObject(Channel channel, ObjectMask object)
            throws FeatureCalculationException {
        return calculateMeanIntensityObject(channel, object, false);
    }

    public static double calculateMeanIntensityObject(
            Channel channel, ObjectMask object, boolean excludeZero)
            throws FeatureCalculationException {
        checkContained(object.boundingBox(), channel.extent());
        
        RunningSum running = new RunningSum();

        IterateVoxelsVoxelBoxAsInt.callEachPoint(channel.voxels().any(), object, (buffer, offset) -> {
            int value = buffer.getInt(offset);

            if (!excludeZero || value != 0) {
                running.increment(value, 1);
            }            
        });

        if (running.getCount() == 0) {
            throw new FeatureCalculationException("There are 0 pixels in the object-mask");
        }

        return running.mean();
    }

    private static void checkContained(BoundingBox box, Extent extent)
            throws FeatureCalculationException {
        if (!extent.contains(box)) {
            throw new FeatureCalculationException(
                    String.format(
                            "The object's bounding-box (%s) is not contained within the dimensions of the channel %s",
                            box, extent));
        }
    }
}
