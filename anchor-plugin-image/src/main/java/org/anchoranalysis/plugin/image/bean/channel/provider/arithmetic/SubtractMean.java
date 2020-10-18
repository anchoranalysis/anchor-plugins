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

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.mask.IterateVoxelsMask;
import org.anchoranalysis.image.core.mask.Mask;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.image.voxel.iterator.IterateVoxelsAll;
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

        Voxels<UnsignedByteBuffer> voxelsIntensity = channel.voxels().asByte();

        double mean = calculateMean(voxelsIntensity, mask);

        int meanRounded = (int) Math.round(mean);

        if (subtractFromMaskOnly) {
            subtractMeanMask(voxelsIntensity, mask, meanRounded);
        } else {
            subtractMeanAll(voxelsIntensity, meanRounded);
        }

        return channel;
    }

    private double calculateMean(Voxels<UnsignedByteBuffer> voxelsIntensity, Mask mask) {
        return IterateVoxelsMask.calculateRunningSum(mask, voxelsIntensity).mean(0);
    }

    private void subtractMeanMask(Voxels<UnsignedByteBuffer> voxelsIntensity, Mask mask, int mean) {
        IterateVoxelsMask.withBuffer(
                mask,
                voxelsIntensity,
                (point, buffer, offset) -> processPoint(buffer, offset, mean));
    }

    private void subtractMeanAll(Voxels<UnsignedByteBuffer> voxelsIntensity, int mean) {
        IterateVoxelsAll.withBuffer(
                voxelsIntensity, (point, buffer, offset) -> processPoint(buffer, offset, mean));
    }

    private static void processPoint(UnsignedByteBuffer buffer, int offset, int mean) {
        int intensity = buffer.getUnsigned(offset);

        int intensitySubtracted = intensity - mean;

        // Clip so it never falls below 0
        if (intensitySubtracted < 0) {
            intensitySubtracted = 0;
        }

        buffer.putUnsigned(offset, intensitySubtracted);
    }
}
