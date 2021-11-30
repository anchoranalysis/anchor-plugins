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
import org.anchoranalysis.core.exception.friendly.AnchorImpossibleSituationException;
import org.anchoranalysis.core.functional.FunctionalIterate;
import org.anchoranalysis.image.core.channel.factory.ChannelFactory;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.dimensions.IncorrectImageSizeException;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.datatype.UnsignedByteVoxelType;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

/**
 * Convert from the OpenCV {@link Mat} to a {@link Stack}.
 *  
 * @author Owen Feehan
 *
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConvertFromMat {

    /**
     * Convert from an OpenCV {@link Mat} to a {@link Stack}.
     * 
     * @param mat either of type {@link CvType#CV_8UC3} for an BGR image, or otherwise a single-channeled image. 
     * @return a newly created {@link Stack}, in RGB order (not BGR) or otherwise single-channeled.
     * @throws OperationFailedException if the data-type is unsupported, or if {@link Mat} has zero width or height, which indicates an error.
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
        Voxels<?> voxels =
                VoxelsSingleChannelFromMat.createVoxelBuffer(mat, dimensionsFrom(mat).extent());
        return new Stack(ChannelFactory.instance().create(voxels));
    }

    /** Converts to a {@link Stack} with three channels (RGB). */
    private static Stack toRGB(Mat mat) throws OperationFailedException {
        Stack stack = createEmptyStack(dimensionsFrom(mat), 3, true);
        VoxelsRGBFromMat.matToRGB(
                mat, stack.getChannel(0), stack.getChannel(1), stack.getChannel(2));
        return stack;
    }

    /** Create a {@link Stack} with a number of newly-created empty (zero-valued) channels. */
    private static Stack createEmptyStack(Dimensions dimensions, int numberChannels, boolean rgb) {
        Stack stack = new Stack(rgb);
        FunctionalIterate.repeat(
                numberChannels,
                () -> {
                    try {
                        stack.addChannel(
                                ChannelFactory.instance()
                                        .create(dimensions, UnsignedByteVoxelType.INSTANCE));
                    } catch (IncorrectImageSizeException e) {
                        throw new AnchorImpossibleSituationException();
                    }
                });
        return stack;
    }

    /** Infer the {@link Dimensions} for an image from a {@link Mat}. */
    private static Dimensions dimensionsFrom(Mat mat) throws OperationFailedException {
        int width = mat.size(1);
        int height = mat.size(0);

        if (width == 0 || height == 0) {
            throw new OperationFailedException(
                    "This file is specified a 0 width or 0 height, which suggests the data-format is not supported.");
        }

        Preconditions.checkArgument(width > 0);
        Preconditions.checkArgument(height > 0);
        return new Dimensions(width, height, 1);
    }
}
