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
import java.util.function.BiConsumer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedShortBuffer;
import org.anchoranalysis.image.voxel.datatype.UnsignedByteVoxelType;
import org.anchoranalysis.image.voxel.datatype.UnsignedShortVoxelType;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.spatial.Extent;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

/**
 * Converts common image data-structures used by Anchor to the {@link Mat} class used by OpenCV.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConvertToMat {

    public static Mat fromObject(ObjectMask object) throws CreateException {
        Extent e = object.boundingBox().extent();
        if (e.z() > 1) {
            throw new CreateException(
                    "Objects with more than 1 z-stack are not supported for OpenCV to Mat conversion (at the moment)");
        }

        return fromSingleChannelByte(object.binaryVoxels().voxels());
    }

    public static Mat fromStack(Stack stack) throws CreateException {

        if (!(stack.getNumberChannels() == 1 || stack.getNumberChannels() == 3)) {
            throw new CreateException("Stack must have 1 or 3 channels");
        }

        if (stack.dimensions().z() > 1) {
            throw new CreateException(
                    "Stacks with more than 1 z-stack are not supported for OpenCV to Mat conversion (at the moment)");
        }

        if (stack.getNumberChannels() == 3) {
            return makeRGBStack(stack, true);
        } else {
            // Single channel
            return makeGrayscale(stack.getChannel(0));
        }
    }

    /**
     * Derives a {@link Mat} representing an RGB stack.
     *
     * @param stack a stack containing three channels
     * @param swapRB if true, the first channel and third channel in {@code stack} are swapped to
     *     make the {@link Mat} to e.g. translate RGB to BGR (as expected by OpenCV).
     * @return a newly created {@link Mat} representation of {@code stack}.
     * @throws CreateException
     */
    public static Mat makeRGBStack(Stack stack, boolean swapRB) throws CreateException {
        if (stack.getNumberChannels() != 3) {
            throw new CreateException("Stack must have 3 channels for RGB conversion");
        }
        VoxelDataType dataType = stack.getChannel(0).getVoxelDataType();
        if (dataType.equals(UnsignedByteVoxelType.INSTANCE)) {
            if (swapRB) {
                return fromRGBByte(stack.getChannel(0), stack.getChannel(1), stack.getChannel(2));
            } else {
                return fromRGBByte(stack.getChannel(2), stack.getChannel(1), stack.getChannel(0));
            }
        } else {
            throw new CreateException("Only unsigned 8-bit channels are supported for RGB");
        }
    }

    public static Mat createEmptyMat(Extent extent, int type) {
        return new Mat(extent.y(), extent.x(), type);
    }

    private static Mat makeGrayscale(Channel channel) throws CreateException {
        if (channel.getVoxelDataType().equals(UnsignedByteVoxelType.INSTANCE)) {
            return fromSingleChannelByte(channel.voxels().asByte());
        } else if (channel.getVoxelDataType().equals(UnsignedShortVoxelType.INSTANCE)) {
            return fromSingleChannelShort(channel.voxels().asShort());
        } else {
            throw new CreateException("Only unsigned 8-bit or 16-bit channels are supported");
        }
    }

    private static Mat fromSingleChannelByte(Voxels<UnsignedByteBuffer> voxels) {
        return fromSingleChannel(
                voxels, CvType.CV_8UC1, (mat, buffer) -> mat.put(0, 0, buffer.array()));
    }

    private static Mat fromSingleChannelShort(Voxels<UnsignedShortBuffer> voxels) {
        return fromSingleChannel(
                voxels, CvType.CV_16UC1, (mat, buffer) -> mat.put(0, 0, buffer.array()));
    }

    private static <T> Mat fromSingleChannel(
            Voxels<T> voxels, int matType, BiConsumer<Mat, T> populateMat) {
        Preconditions.checkArgument(voxels.extent().z() == 1);

        Mat mat = createEmptyMat(voxels.extent(), matType);
        populateMat.accept(mat, voxels.sliceBuffer(0));
        return mat;
    }

    private static Mat fromRGBByte(Channel channelRed, Channel channelGreen, Channel channelBlue) {

        Extent extent = channelRed.extent();
        Preconditions.checkArgument(extent.z() == 1);

        Mat mat = createEmptyMat(channelRed.extent(), CvType.CV_8UC3);

        UnsignedByteBuffer red = BufferHelper.extractByte(channelRed);
        UnsignedByteBuffer green = BufferHelper.extractByte(channelGreen);
        UnsignedByteBuffer blue = BufferHelper.extractByte(channelBlue);

        extent.iterateOverXY(
                point -> {
                    // Note BGR format in OpenCV
                    byte[] colArr = new byte[] {blue.getRaw(), green.getRaw(), red.getRaw()};
                    mat.put(point.y(), point.x(), colArr);
                });
        return mat;
    }
}
