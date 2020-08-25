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

package org.anchoranalysis.plugin.image.bean.channel.provider.arithmetic;

import java.nio.ByteBuffer;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.convert.ByteConverter;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.iterator.IterateVoxels;
import org.anchoranalysis.image.voxel.iterator.IterateVoxelsByte;
import org.anchoranalysis.plugin.image.bean.channel.provider.mask.UnaryWithMaskBase;

/**
 * Subtract the mean (of the entire channel or a masked portion thereof) from every voxel
 *
 * @author Owen Feehan
 */
public class SubtractMean extends UnaryWithMaskBase {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private boolean subtractFromMaskOnly = true;
    // END BEAN PROPERTIES

    @Override
    protected Channel createFromMaskedChannel(Channel channel, Mask mask) throws CreateException {

        Voxels<ByteBuffer> voxelsIntensity = channel.voxels().asByte();

        double mean = calculateMean(voxelsIntensity, mask);

        int meanRounded = (int) Math.round(mean);

        if (subtractFromMaskOnly) {
            subtractMeanMask(voxelsIntensity, mask, meanRounded);
        } else {
            subtractMeanAll(voxelsIntensity, meanRounded);
        }

        return channel;
    }

    private double calculateMean(Voxels<ByteBuffer> voxelsIntensity, Mask mask) {
        return IterateVoxelsByte.calculateSumAndCount(voxelsIntensity, mask).mean(0);
    }

    private void subtractMeanMask(Voxels<ByteBuffer> voxelsIntensity, Mask mask, int mean) {
        IterateVoxels.callEachPoint(
                voxelsIntensity,
                mask,
                (point, buffer, offset) -> processPoint(buffer, offset, mean));
    }

    private void subtractMeanAll(Voxels<ByteBuffer> voxelsIntensity, int mean) {
        IterateVoxels.callEachPoint(
                voxelsIntensity, (point, buffer, offset) -> processPoint(buffer, offset, mean));
    }

    private static void processPoint(ByteBuffer buffer, int offset, int mean) {
        int intensity = ByteConverter.unsignedByteToInt(buffer.get(offset));

        int intensitySubtracted = intensity - mean;

        // Clip so it never falls below 0
        if (intensitySubtracted < 0) {
            intensitySubtracted = 0;
        }

        buffer.put(offset, (byte) intensitySubtracted);
    }
}
