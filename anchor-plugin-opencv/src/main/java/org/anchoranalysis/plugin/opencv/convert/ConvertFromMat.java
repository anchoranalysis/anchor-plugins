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
import java.nio.Buffer;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.exception.friendly.AnchorImpossibleSituationException;
import org.anchoranalysis.core.functional.FunctionalIterate;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.channel.factory.ChannelFactory;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.dimensions.IncorrectImageSizeException;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.buffer.VoxelBufferWrap;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.image.voxel.datatype.UnsignedByteVoxelType;
import org.anchoranalysis.image.voxel.factory.VoxelsFactory;
import org.anchoranalysis.image.voxel.factory.VoxelsFactoryTypeBound;
import org.anchoranalysis.spatial.Extent;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConvertFromMat {

    private static final OpenCVFrameConverter.ToMat CONVERTER = new OpenCVFrameConverter.ToMat();

    public static Stack toStack(Mat mat) throws OperationFailedException {

        if (mat.type() == CvType.CV_8UC1) {
            return toGrayscale(
                    mat, VoxelBufferWrap::unsignedByteRaw, VoxelsFactory.getUnsignedByte());
        } else if (mat.type() == CvType.CV_16UC1) {
            return toGrayscale(
                    mat, VoxelBufferWrap::unsignedShortRaw, VoxelsFactory.getUnsignedShort());
        } else if (mat.type() == CvType.CV_8UC3) {
            return toRGB(mat);
        } else {
            throw new OperationFailedException(
                    "Only unsigned 8-bit grayscale and RGB images are current supported for conversion");
        }
    }

    private static <T extends Buffer, S> Stack toGrayscale(
            Mat mat,
            Function<T, VoxelBuffer<S>> voxelBufferFromBuffer,
            VoxelsFactoryTypeBound<S> factory) {

        org.bytedeco.opencv.opencv_core.Mat matConverted =
                CONVERTER.convertToMat(CONVERTER.convert(mat));

        Voxels<S> voxels = factory.createForBuffer(
                createVoxelBuffer(matConverted, voxelBufferFromBuffer),
                dimensionsFrom(mat).extent());

        return new Stack(ChannelFactory.instance().create(voxels));
    }
    
    private static <T extends Buffer, S> VoxelBuffer<S> createVoxelBuffer(org.bytedeco.opencv.opencv_core.Mat matConverted, Function<T, VoxelBuffer<S>> voxelBufferFromBuffer) {
        try {
            T bufferArrayBacked = ConvertDirectBuffer.convertIfNeeded(matConverted.createBuffer());
            return voxelBufferFromBuffer.apply(bufferArrayBacked);
        } catch (OperationFailedException e) {
            throw new AnchorImpossibleSituationException();
        }
    }

    private static Stack toRGB(Mat mat) {
        Stack stack = createEmptyStack(dimensionsFrom(mat), 3);
        matToRGB(mat, stack.getChannel(0), stack.getChannel(1), stack.getChannel(2));
        return stack;
    }

    private static void matToRGB(
            Mat mat, Channel channelRed, Channel channelGreen, Channel channelBlue) {

        Extent extent = channelRed.extent();
        Preconditions.checkArgument(extent.z() == 1);

        UnsignedByteBuffer red = BufferHelper.extractByte(channelRed);
        UnsignedByteBuffer green = BufferHelper.extractByte(channelGreen);
        UnsignedByteBuffer blue = BufferHelper.extractByte(channelBlue);

        byte[] arr = new byte[3];

        for (int y = 0; y < extent.y(); y++) {
            for (int x = 0; x < extent.x(); x++) {

                mat.get(y, x, arr);

                // OpenCV uses a BGR order as opposed to RGB in Anchor.
                blue.putRaw(arr[0]);
                green.putRaw(arr[1]);
                red.putRaw(arr[2]);
            }
        }

        assert (!red.hasRemaining());
        assert (!green.hasRemaining());
        assert (!blue.hasRemaining());
    }

    private static Stack createEmptyStack(Dimensions dimensions, int numberChannels) {
        Stack stack = new Stack();
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

    private static Dimensions dimensionsFrom(Mat mat) {
        return new Dimensions(mat.size(1), mat.size(0), 1);
    }
}
