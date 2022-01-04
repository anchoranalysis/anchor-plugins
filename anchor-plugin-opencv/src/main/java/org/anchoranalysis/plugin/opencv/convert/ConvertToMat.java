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
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.function.BiConsumer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedShortBuffer;
import org.anchoranalysis.image.voxel.datatype.UnsignedByteVoxelType;
import org.anchoranalysis.image.voxel.datatype.UnsignedShortVoxelType;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.spatial.box.Extent;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

/**
 * Converts common image data-structures used by Anchor to the {@link Mat} class used by OpenCV.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConvertToMat {

    /**
     * Convert a {@link ObjectMask} to a {@link Mat}.
     *
     * @param object the object to convert.
     * @return a newly created {@link Mat} containing the voxels in the mask of the {@link
     *     ObjectMask}.
     * @throws CreateException if the object is 3D, which is unsupported.
     */
    public static Mat fromObject(ObjectMask object) throws CreateException {
        Extent extent = object.boundingBox().extent();
        if (extent.z() > 1) {
            throw new CreateException(
                    "Objects with more than 1 z-slice are not supported for OpenCV to Mat conversion (at the moment)");
        }

        return fromVoxelsByte(object.binaryVoxels().voxels());
    }

    /**
     * Converts a {@link Stack} to a {@link Mat}.
     *
     * @param stack the stack to convert, which must have 1 or 3 channels (in which case, it is
     *     presumed to be RGB).
     * @return a newly-created {@link Mat} with identical voxels to {@code Stack}. In the case of an
     *     RGB image, the {@link Mat} has BGR channel ordering.
     * @throws CreateException if the stack is 3D, or has an invalid number of channels.
     */
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
     * Converts a {@link Voxels} of type {@link UnsignedByteBuffer} to a {@link Mat}.
     *
     * @param voxels the voxels to convert.
     * @return a newly created {@link Mat}.
     */
    public static Mat fromVoxelsByte(Voxels<UnsignedByteBuffer> voxels) {
        return fromVoxels(voxels, CvType.CV_8UC1, (mat, buffer) -> mat.put(0, 0, buffer.array()));
    }

    /**
     * Converts a {@link Voxels} of type {@link UnsignedShortBuffer} to a {@link Mat}.
     *
     * @param voxels the voxels to convert.
     * @return a newly created {@link Mat}.
     */
    public static Mat fromVoxelsShort(Voxels<UnsignedShortBuffer> voxels) {
        return fromVoxels(voxels, CvType.CV_16UC1, (mat, buffer) -> mat.put(0, 0, buffer.array()));
    }

    /**
     * Converts a {@link Voxels} of type {@link FloatBuffer} to a {@link Mat}.
     *
     * @param voxels the voxels to convert.
     * @return a newly created {@link Mat}.
     */
    public static Mat fromVoxelsFloat(Voxels<FloatBuffer> voxels) {
        return fromVoxels(voxels, CvType.CV_32FC1, (mat, buffer) -> mat.put(0, 0, buffer.array()));
    }

    /**
     * Converts a {@link VoxelBuffer} of type {@link UnsignedByteBuffer} to a {@link Mat}.
     *
     * @param voxelBuffer the voxel-buffer to convert.
     * @param extent the size of the image the buffer represents (must have identical number of
     *     voxels to {@code voxelBuffer}.
     * @return a newly created {@link Mat}.
     */
    public static Mat fromVoxelBufferByte(
            VoxelBuffer<UnsignedByteBuffer> voxelBuffer, Extent extent) {
        return fromVoxelBuffer(
                voxelBuffer,
                extent,
                CvType.CV_8UC1,
                (mat, buffer) -> mat.put(0, 0, buffer.array()));
    }

    /**
     * Converts a {@link VoxelBuffer} of type {@link UnsignedShortBuffer} to a {@link Mat}.
     *
     * @param voxelBuffer the voxel-buffer to convert.
     * @param extent the size of the image the buffer represents (must have identical number of
     *     voxels to {@code voxelBuffer}.
     * @return a newly created {@link Mat}.
     */
    public static Mat fromVoxelBufferShort(
            VoxelBuffer<UnsignedShortBuffer> voxelBuffer, Extent extent) {
        return fromVoxelBuffer(
                voxelBuffer,
                extent,
                CvType.CV_16UC1,
                (mat, buffer) -> mat.put(0, 0, buffer.array()));
    }

    /**
     * Converts a {@link VoxelBuffer} of type {@link FloatBuffer} to a {@link Mat}.
     *
     * @param voxelBuffer the voxel-buffer to convert.
     * @param extent the size of the image the buffer represents (must have identical number of
     *     voxels to {@code voxelBuffer}.
     * @return a newly created {@link Mat}.
     */
    public static Mat fromVoxelBufferFloat(VoxelBuffer<FloatBuffer> voxelBuffer, Extent extent) {
        return fromVoxelBuffer(
                voxelBuffer,
                extent,
                CvType.CV_32FC1,
                (mat, buffer) -> mat.put(0, 0, buffer.array()));
    }

    /**
     * Derives a {@link Mat} representing an RGB stack.
     *
     * @param stack a stack containing three channels.
     * @param swapRedBlueChannels if true, the first channel and third channel in {@code stack} are
     *     swapped to make the {@link Mat} to e.g. translate RGB to BGR (as expected by OpenCV).
     * @return a newly created {@link Mat} representation of {@code stack}.
     * @throws CreateException if the stack does not have exactly three channels.
     */
    public static Mat makeRGBStack(Stack stack, boolean swapRedBlueChannels)
            throws CreateException {
        if (stack.getNumberChannels() != 3) {
            throw new CreateException("Stack must have 3 channels for RGB conversion");
        }
        VoxelDataType dataType = stack.getChannel(0).getVoxelDataType();
        if (dataType.equals(UnsignedByteVoxelType.INSTANCE)) {
            if (swapRedBlueChannels) {
                return fromRGBByte(stack.getChannel(0), stack.getChannel(1), stack.getChannel(2));
            } else {
                return fromRGBByte(stack.getChannel(2), stack.getChannel(1), stack.getChannel(0));
            }
        } else {
            throw new CreateException("Only unsigned 8-bit channels are supported for RGB");
        }
    }

    /**
     * Creates a {@link Mat} which contains only zero-values.
     *
     * @param extent the size of the {@link Mat} to create.
     * @param type a OpenCV type constant indicating the data-type of the voxels in {@link Mat}.
     * @return the newly created {@link Mat}.
     */
    public static Mat createEmptyMat(Extent extent, int type) {
        return new Mat(extent.y(), extent.x(), type);
    }

    private static Mat makeGrayscale(Channel channel) throws CreateException {
        if (channel.getVoxelDataType().equals(UnsignedByteVoxelType.INSTANCE)) {
            return fromVoxelsByte(channel.voxels().asByte());
        } else if (channel.getVoxelDataType().equals(UnsignedShortVoxelType.INSTANCE)) {
            return fromVoxelsShort(channel.voxels().asShort());
        } else {
            throw new CreateException("Only unsigned 8-bit or 16-bit channels are supported");
        }
    }

    private static <T> Mat fromVoxels(
            Voxels<T> voxels, int matType, BiConsumer<Mat, T> populateMat) {
        Preconditions.checkArgument(voxels.extent().z() == 1);

        Mat mat = createEmptyMat(voxels.extent(), matType);
        populateMat.accept(mat, voxels.sliceBuffer(0));
        return mat;
    }

    private static <T> Mat fromVoxelBuffer(
            VoxelBuffer<T> voxelBuffer,
            Extent extent,
            int matType,
            BiConsumer<Mat, T> populateMat) {
        Mat mat = createEmptyMat(extent, matType);
        populateMat.accept(mat, voxelBuffer.buffer());
        return mat;
    }

    private static Mat fromRGBByte(Channel channelRed, Channel channelGreen, Channel channelBlue) {

        Extent extent = channelRed.extent();
        Preconditions.checkArgument(extent.z() == 1);

        Mat mat = createEmptyMat(channelRed.extent(), CvType.CV_8UC3);

        UnsignedByteBuffer red = BufferHelper.extractByte(channelRed);
        UnsignedByteBuffer green = BufferHelper.extractByte(channelGreen);
        UnsignedByteBuffer blue = BufferHelper.extractByte(channelBlue);

        // Its quicker to write all bytes at once to the OpenCV matrix
        ByteBuffer out = ByteBuffer.allocate(channelRed.extent().areaXY() * 3);
        while (red.hasRemaining()) {
            out.put(blue.getRaw());
            out.put(green.getRaw());
            out.put(red.getRaw());
        }
        mat.put(0, 0, out.array());

        return mat;
    }
}
