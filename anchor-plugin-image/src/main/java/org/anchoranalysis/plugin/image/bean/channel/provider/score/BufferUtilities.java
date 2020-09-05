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

package org.anchoranalysis.plugin.image.bean.channel.provider.score;

import org.anchoranalysis.image.convert.UnsignedByteBuffer;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.image.feature.bean.score.VoxelScore;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class BufferUtilities {

    public static void putScoreForOffset(
            VoxelScore pixelScore, List<VoxelBuffer<?>> bufferList, UnsignedByteBuffer bufferOut, int offset)
            throws FeatureCalculationException {
        double score = pixelScore.calculate(createParams(bufferList, offset));
        bufferOut.putUnsigned(offset, (int) Math.round(score * 255));
    }

    private static int[] createParams(List<VoxelBuffer<?>> list, int offset) {

        int[] vals = new int[list.size()];

        for (int index = 0; index < list.size(); index++) {
            vals[index] = list.get(index).getInt(offset);
        }

        return vals;
    }
}
