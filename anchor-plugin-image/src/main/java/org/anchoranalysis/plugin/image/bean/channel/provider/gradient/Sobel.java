/*-
 * #%L
 * anchor-plugin-image
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

package org.anchoranalysis.plugin.image.bean.channel.provider.gradient;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.NativeImg;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.outofbounds.OutOfBounds;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.convert.ImgLib2Wrap;
import org.anchoranalysis.image.extent.Dimensions;
import org.anchoranalysis.image.voxel.datatype.FloatVoxelType;
import org.anchoranalysis.image.voxel.datatype.UnsignedByteVoxelType;
import org.anchoranalysis.image.voxel.datatype.UnsignedShortVoxelType;

/**
 * 3x3 Sobel Filter
 *
 * @author Owen Feehan
 */
public class Sobel extends GradientBase {

    @Override
    public Channel createFromChannel(Channel channelIn) throws CreateException {

        Channel intermediate = createNewFloat(channelIn.dimensions());

        processMultiplexInputType(
                channelIn, ImgLib2Wrap.wrapFloat(intermediate.voxels().asFloat()));

        // convert to our output from the float
        return convertToOutputType(intermediate);
    }

    private static Channel createNewFloat(Dimensions dimensions) {
        return ChannelFactory.instance().create(dimensions, FloatVoxelType.INSTANCE);
    }

    private void processMultiplexInputType(Channel channelIn, NativeImg<FloatType, FloatArray> out)
            throws CreateException {
        if (channelIn.getVoxelDataType().equals(UnsignedByteVoxelType.INSTANCE)) {
            process(
                    ImgLib2Wrap.wrapByte(channelIn.voxels().asByte()),
                    out,
                    (float) getScaleFactor());
        } else if (channelIn.getVoxelDataType().equals(UnsignedShortVoxelType.INSTANCE)) {
            process(
                    ImgLib2Wrap.wrapShort(channelIn.voxels().asShort()),
                    out,
                    (float) getScaleFactor());
        } else {
            throw new CreateException("Input type must be unsigned byte or short");
        }
    }

    // From netlib
    // https://github.com/imglib/imglib2-algorithm-gpl/blob/master/src/main/java/net/imglib2/algorithm/pde/Gradient.java
    private static <T extends RealType<T>> void process(
            NativeImg<T, ?> input, NativeImg<FloatType, FloatArray> output, float scaleFactor) {

        Cursor<T> in = Views.iterable(input).localizingCursor();
        RandomAccess<FloatType> oc = output.randomAccess();
        OutOfBounds<T> ra = Views.extendMirrorDouble(input).randomAccess();

        float[][] kernel = new float[3][3];

        while (in.hasNext()) {
            in.fwd();

            // Position neighborhood cursor
            ra.setPosition(in);

            // Position output cursor
            for (int i = 0; i < input.numDimensions(); i++) {
                oc.setPosition(in.getLongPosition(i), i);
            }

            // X Column=-1
            ra.bck(0);

            ra.bck(1);
            kernel[0][0] = ra.get().getRealFloat();
            ra.fwd(1);
            kernel[0][1] = ra.get().getRealFloat();
            ra.fwd(1);
            kernel[0][2] = ra.get().getRealFloat();
            ra.bck(1);

            // X Column=0
            ra.fwd(0);

            ra.bck(1);
            kernel[1][0] = ra.get().getRealFloat();
            ra.fwd(1);
            kernel[1][1] = ra.get().getRealFloat();
            ra.fwd(1);
            kernel[1][2] = ra.get().getRealFloat();
            ra.bck(1);

            // X Column=+1
            ra.fwd(0);
            ra.bck(1);
            kernel[2][0] = ra.get().getRealFloat();
            ra.fwd(1);
            kernel[2][1] = ra.get().getRealFloat();
            ra.fwd(1);
            kernel[2][2] = ra.get().getRealFloat();
            ra.bck(1);

            ra.bck(0);

            // https://en.wikipedia.org/wiki/Sobel_operator
            float gx =
                    -1 * kernel[0][0]
                            - 2 * kernel[0][1]
                            - 1 * kernel[0][2]
                            + 1 * kernel[2][0]
                            + 2 * kernel[2][1]
                            + 1 * kernel[2][2];
            float gy =
                    -1 * kernel[0][0]
                            - 2 * kernel[1][0]
                            - 1 * kernel[2][0]
                            + 1 * kernel[0][2]
                            + 2 * kernel[1][2]
                            + 1 * kernel[2][2];

            float diffNorm = (float) Math.sqrt(Math.pow(gx, 2.0) + Math.pow(gy, 2.0));
            oc.get().set(diffNorm * scaleFactor);
        }
    }
}
