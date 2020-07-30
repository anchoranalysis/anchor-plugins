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

package org.anchoranalysis.plugin.opencv;

import java.nio.ByteBuffer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MatConverter {

    public static Mat fromObject(ObjectMask object) throws CreateException {
        Extent e = object.getBoundingBox().extent();
        if (e.getZ() > 1) {
            throw new CreateException(
                    "Objects with more than 1 z-stack are not supported for OpenCV to Mat conversion (at the moment)");
        }

        return singleChnlMatFromVoxelBox(object.binaryVoxelBox().getVoxelBox());
    }

    public static Mat fromStack(Stack stack) throws CreateException {

        if (!(stack.getNumberChannels() == 1 || stack.getNumberChannels() == 3)) {
            throw new CreateException("Stack must have 1 or 3 channels");
        }

        if (stack.getDimensions().getZ() > 1) {
            throw new CreateException(
                    "Stacks with more than 1 z-stack are not supported for OpenCV to Mat conversion (at the moment)");
        }

        if (stack.getNumberChannels() == 3) {
            return makeRGBStack(stack);
        } else {
            // Single channel
            return makeGrayscale(stack.getChannel(0));
        }
    }

    public static Mat makeRGBStack(Stack stack) throws CreateException {
        if (stack.getNumberChannels() != 3) {
            throw new CreateException("Stack must have 3 channels for RGB conversion");
        }
        return matFromRGB(stack.getChannel(0), stack.getChannel(1), stack.getChannel(2));
    }

    public static void matToRGB(Mat mat, Stack stack) throws CreateException {
        if (stack.getNumberChannels() != 3) {
            throw new CreateException("Stack must have 3 channels for RGB conversion");
        }
        matToRGB(mat, stack.getChannel(0), stack.getChannel(1), stack.getChannel(2));
    }

    private static Mat makeGrayscale(Channel chnl) throws CreateException {
        if (chnl.getVoxelDataType().equals(VoxelDataTypeUnsignedByte.INSTANCE)) {
            return singleChnlMatFromVoxelBox(chnl.getVoxelBox().asByte());
        } else {
            throw new CreateException("Only 8-bit channels are supported");
        }
    }

    private static Mat singleChnlMatFromVoxelBox(VoxelBox<ByteBuffer> vb) {

        assert (vb.extent().getZ()) == 1;

        Mat mat = createEmptyMat(vb.extent(), CvType.CV_8UC1);
        mat.put(0, 0, vb.getPixelsForPlane(0).buffer().array());
        return mat;
    }

    private static Mat matFromRGB(Channel chnlRed, Channel chnlGreen, Channel chnlBlue) {

        Extent e = chnlRed.getDimensions().getExtent();
        assert (e.getZ()) == 1;

        Mat mat = createEmptyMat(chnlRed.getDimensions().getExtent(), CvType.CV_8UC3);

        ByteBuffer red = bufferFromChnl(chnlRed);
        ByteBuffer green = bufferFromChnl(chnlGreen);
        ByteBuffer blue = bufferFromChnl(chnlBlue);

        for (int y = 0; y < e.getY(); y++) {
            for (int x = 0; x < e.getX(); x++) {

                // Note BGR format in OpenCV
                byte[] colArr = new byte[] {blue.get(), green.get(), red.get()};
                mat.put(y, x, colArr);
            }
        }

        assert (!red.hasRemaining());
        assert (!green.hasRemaining());
        assert (!blue.hasRemaining());

        return mat;
    }

    private static void matToRGB(Mat mat, Channel chnlRed, Channel chnlGreen, Channel chnlBlue) {

        Extent e = chnlRed.getDimensions().getExtent();
        assert (e.getZ()) == 1;

        ByteBuffer red = bufferFromChnl(chnlRed);
        ByteBuffer green = bufferFromChnl(chnlGreen);
        ByteBuffer blue = bufferFromChnl(chnlBlue);

        byte[] arr = new byte[3];

        for (int y = 0; y < e.getY(); y++) {
            for (int x = 0; x < e.getX(); x++) {

                mat.get(y, x, arr);

                red.put(arr[0]);
                green.put(arr[1]);
                blue.put(arr[2]);
            }
        }

        assert (!red.hasRemaining());
        assert (!green.hasRemaining());
        assert (!blue.hasRemaining());
    }

    private static ByteBuffer bufferFromChnl(Channel chnl) {
        return chnl.getVoxelBox().asByte().getPixelsForPlane(0).buffer();
    }

    public static Mat createEmptyMat(Extent e, int type) {
        return new Mat(e.getY(), e.getX(), type);
    }
}
