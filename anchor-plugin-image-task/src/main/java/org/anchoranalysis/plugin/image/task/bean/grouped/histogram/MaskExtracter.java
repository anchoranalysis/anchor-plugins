/*-
 * #%L
 * anchor-plugin-image-task
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

package org.anchoranalysis.plugin.image.task.bean.grouped.histogram;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Optional;

import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.mask.Mask;
import org.anchoranalysis.image.voxel.binary.values.BinaryValuesInt;
import org.anchoranalysis.plugin.image.task.grouped.ChannelSource;

/** Extracts a histogram from an image for a given key */
@NoArgsConstructor(access=AccessLevel.PRIVATE)
class MaskExtracter {

    public static Optional<Mask> extractMask(ChannelSource source, String maskStackName, int maskValue) throws OperationFailedException {
    	if (!maskStackName.isEmpty()) {
            Channel extracted = source.extractChannel(maskStackName, false);
            return Optional.of( new Mask(extracted, createMaskBinaryValues(maskValue)) );
    	} else {
    		return Optional.empty();
    	}
    }

    private static BinaryValuesInt createMaskBinaryValues(int maskValue) throws OperationFailedException {
        if (maskValue == 255) {
            return new BinaryValuesInt(0, 255);
        } else if (maskValue == 0) {
            return new BinaryValuesInt(255, 0);
        } else {
            throw new OperationFailedException("Only mask-values of 255 or 0 are current supported");
        }
    }
}
