/*-
 * #%L
 * anchor-plugin-opencv
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
package org.anchoranalysis.plugin.opencv.convert;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.exception.friendly.AnchorImpossibleSituationException;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.dimensions.IncorrectImageSizeException;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.spatial.box.Extent;
import org.opencv.core.Core;
import org.opencv.core.Mat;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class VoxelsRGBFromMat {

    /**
     * Assigns values to three {@link Channel}s from a {@link Mat} containing BGR voxels.
     *
     * <p>All {@link Channel}s must be the same size, and {@code mat} should contain a number of
     * elements that is exactly three times an individual {@link Channel}'s size.
     *
     * @param mat the mat containing unsigned-byte voxels, interleaved for the channels in BGR
     *     order.
     * @param extent the size of a channel.
     * @return a newly created {@link Stack}, containing three channels, in respective RGB order.
     * @throws OperationFailedException if the {@link Mat} contains an invalid channel type, or
     *     otherwise unable to complete successfully.
     */
    public static Stack matToRGB(Mat mat, Extent extent) throws OperationFailedException {

        Preconditions.checkArgument(extent.z() == 1);

        List<Mat> split = new ArrayList<>(3);
        Core.split(mat, split);

        try {
            return new Stack(
                    true,
                    VoxelsFromMat.toChannel(split.get(2), extent),
                    VoxelsFromMat.toChannel(split.get(1), extent),
                    VoxelsFromMat.toChannel(split.get(0), extent));
        } catch (IncorrectImageSizeException e) {
            throw new AnchorImpossibleSituationException();
        } catch (CreateException e) {
            throw new OperationFailedException(
                    "Unable to create a stack from the the three extacted channels", e);
        }
    }
}
