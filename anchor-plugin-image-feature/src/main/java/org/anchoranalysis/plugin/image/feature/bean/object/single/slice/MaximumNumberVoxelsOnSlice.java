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

package org.anchoranalysis.plugin.image.feature.bean.object.single.slice;

import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.FeatureCalculationInput;
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.image.voxel.object.ObjectMask;

/**
 * Maximum number of voxels in the object-mask across all slices.
 *
 * @author Owen Feehan
 */
public class MaximumNumberVoxelsOnSlice extends FeatureSingleObject {

    @Override
    public double calculate(FeatureCalculationInput<FeatureInputSingleObject> input)
            throws FeatureCalculationException {

        ObjectMask single = input.get().getObject();

        byte onByte = single.binaryValuesByte().getOn();

        int max = 0;

        for (int z = 0; z < single.boundingBox().extent().z(); z++) {
            UnsignedByteBuffer buffer = single.sliceBufferLocal(z);
            int count = countForByteBuffer(buffer, onByte);

            if (count > max) {
                max = count;
            }
        }
        return max;
    }

    private static int countForByteBuffer(UnsignedByteBuffer buffer, byte equalValue) {
        int count = 0;
        while (buffer.hasRemaining()) {
            if (buffer.getRaw() == equalValue) {
                count++;
            }
        }
        return count;
    }
}
