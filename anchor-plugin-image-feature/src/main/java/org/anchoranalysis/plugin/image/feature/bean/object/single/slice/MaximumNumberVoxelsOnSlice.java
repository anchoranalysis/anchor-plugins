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

import java.nio.ByteBuffer;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalculationException;
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;

/**
 * Maximum number of voxels in the object-mask across all slices.
 *
 * @author Owen Feehan
 */
public class MaximumNumberVoxelsOnSlice extends FeatureSingleObject {

    @Override
    public double calc(SessionInput<FeatureInputSingleObject> input)
            throws FeatureCalculationException {

        FeatureInputSingleObject params = input.get();

        int max = 0;

        for (int z = 0; z < params.getObject().getBoundingBox().extent().getZ(); z++) {
            ByteBuffer bb = params.getObject().getVoxelBox().getPixelsForPlane(z).buffer();
            int cnt = cntForByteBuffer(bb, params.getObject().getBinaryValuesByte().getOnByte());

            if (cnt > max) {
                max = cnt;
            }
        }
        return max;
    }

    private static int cntForByteBuffer(ByteBuffer bb, byte equalVal) {
        int cnt = 0;
        while (bb.hasRemaining()) {
            if (bb.get() == equalVal) {
                cnt++;
            }
        }
        return cnt;
    }
}
