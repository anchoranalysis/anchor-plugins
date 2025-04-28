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

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.mask.Mask;
import org.anchoranalysis.image.voxel.binary.values.BinaryValuesInt;
import org.anchoranalysis.plugin.image.task.grouped.ChannelSource;

/** Extracts a mask from an image for a given key. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class MaskExtracter {

    /**
     * Extracts a mask from a channel source.
     *
     * @param source the {@link ChannelSource} to extract the channel from.
     * @param maskStackName the name of the mask stack.
     * @param maskValue the value to use for the mask (either 0 or 255).
     * @return an {@link Optional} containing the extracted {@link Mask}, or empty if the
     *     maskStackName is empty.
     * @throws OperationFailedException if the operation fails.
     */
    public static Optional<Mask> extractMask(
            ChannelSource source, String maskStackName, int maskValue)
            throws OperationFailedException {
        if (!maskStackName.isEmpty()) {
            Channel extracted = source.extractChannel(maskStackName, false);
            return Optional.of(new Mask(extracted, createMaskBinaryValues(maskValue)));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Creates binary values for the mask based on the given mask value.
     *
     * @param maskValue the value to use for the mask (either 0 or 255).
     * @return a {@link BinaryValuesInt} object representing the binary values for the mask.
     * @throws OperationFailedException if the maskValue is not 0 or 255.
     */
    private static BinaryValuesInt createMaskBinaryValues(int maskValue)
            throws OperationFailedException {
        return switch (maskValue) {
            case 255 -> new BinaryValuesInt(0, 255);
            case 0 -> new BinaryValuesInt(255, 0);
            default ->
                    throw new OperationFailedException(
                            "Only mask-values of 255 or 0 are currently supported");
        };
    }
}
