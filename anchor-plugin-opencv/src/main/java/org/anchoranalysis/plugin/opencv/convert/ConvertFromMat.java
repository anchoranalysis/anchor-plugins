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
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.core.channel.factory.ChannelFactory;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.spatial.box.Extent;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

/**
 * Convert from the OpenCV {@link Mat} to a {@link Stack}.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConvertFromMat {

    /**
     * Convert from an OpenCV {@link Mat} to a {@link Stack}.
     *
     * @param mat either of type {@link CvType#CV_8UC3} for an BGR image, or otherwise a
     *     single-channeled image.
     * @return a newly created {@link Stack}, in RGB order (not BGR) or otherwise single-channeled.
     * @throws OperationFailedException if the data-type is unsupported, or if {@link Mat} has zero
     *     width or height, which indicates an error.
     */
    public static Stack toStack(Mat mat) throws OperationFailedException {
        if (mat.type() == CvType.CV_8UC3) {
            return toRGB(mat);
        } else {
            return toGrayscale(mat);
        }
    }

    /** Converts to a {@link Stack} with a single channel. */
    private static Stack toGrayscale(Mat mat) throws OperationFailedException {
        Voxels<?> voxels = VoxelsFromMat.toVoxels(mat, extentFrom(mat));
        return new Stack(ChannelFactory.instance().create(voxels));
    }

    /** Converts to a {@link Stack} with three channels (RGB). */
    private static Stack toRGB(Mat mat) throws OperationFailedException {
        return VoxelsRGBFromMat.matToRGB(mat, extentFrom(mat));
    }

    /** Infer the {@link Dimensions} for an image from a {@link Mat}. */
    private static Extent extentFrom(Mat mat) throws OperationFailedException {
        int width = mat.size(1);
        int height = mat.size(0);

        if (width == 0 || height == 0) {
            throw new OperationFailedException(
                    "OpenCV indicated a width or height of 0 for this file, which suggests the data-format is not supported.");
        }

        Preconditions.checkArgument(width > 0);
        Preconditions.checkArgument(height > 0);
        return new Extent(width, height, 1);
    }
}
