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
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.spatial.box.Extent;
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
     * @param channelRed the <b>red</b> channel to assign voxels to.
     * @param channelGreen the <b>blue</b> channel to assign voxels to.
     * @param channelBlue the <b>green</b> channel to assign voxels to.
     */
    public static void matToRGB(
            Mat mat, Channel channelRed, Channel channelGreen, Channel channelBlue) {

        Extent extent = channelRed.extent();
        Preconditions.checkArgument(extent.z() == 1);

        UnsignedByteBuffer red = BufferHelper.extractByte(channelRed);
        UnsignedByteBuffer green = BufferHelper.extractByte(channelGreen);
        UnsignedByteBuffer blue = BufferHelper.extractByte(channelBlue);

        ByteBuffer buffer =
                ByteBufferFromNativeAddress.wrapAddress(
                        mat.dataAddr(), channelRed.extent().areaXY() * 3);

        while (buffer.hasRemaining()) {
            // OpenCV uses a BGR order as opposed to RGB in Anchor.
            blue.putRaw(buffer.get());
            green.putRaw(buffer.get());
            red.putRaw(buffer.get());
        }

        assert (!red.hasRemaining());
        assert (!green.hasRemaining());
        assert (!blue.hasRemaining());

        blue.rewind();
        red.rewind();
        green.rewind();
    }
}
