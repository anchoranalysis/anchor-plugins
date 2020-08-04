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

package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import java.nio.FloatBuffer;
import lombok.Getter;
import lombok.Setter;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.NativeImg;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.outofbounds.OutOfBounds;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProviderOne;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.convert.ImgLib2Wrap;
import org.anchoranalysis.image.stack.region.chnlconverter.ChannelConverter;
import org.anchoranalysis.image.stack.region.chnlconverter.ChannelConverterToUnsignedByte;
import org.anchoranalysis.image.stack.region.chnlconverter.ChannelConverterToUnsignedShort;
import org.anchoranalysis.image.stack.region.chnlconverter.ConversionPolicy;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeFloat;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedShort;

// 3x3 Sobel Filter
public class ChnlProviderEdgeFilter extends ChnlProviderOne {

    // START BEAN
    @BeanField @Getter @Setter private double scaleFactor = 1.0;

    @BeanField @Getter @Setter
    private boolean outputShort = false; // If true, outputs a short. Otherwise a byte
    // END BEAN

    @Override
    public Channel createFromChnl(Channel chnlIn) throws CreateException {

        Channel chnlIntermediate =
                ChannelFactory.instance()
                        .createEmptyInitialised(
                                chnlIn.dimensions(), VoxelDataTypeFloat.INSTANCE);
        Voxels<FloatBuffer> voxels = chnlIntermediate.voxels().asFloat();

        NativeImg<FloatType, FloatArray> natOut = ImgLib2Wrap.wrapFloat(voxels);

        if (chnlIn.getVoxelDataType().equals(VoxelDataTypeUnsignedByte.INSTANCE)) {
            NativeImg<UnsignedByteType, ByteArray> natIn =
                    ImgLib2Wrap.wrapByte(chnlIn.voxels().asByte());
            process(natIn, natOut, (float) scaleFactor);
        } else if (chnlIn.getVoxelDataType().equals(VoxelDataTypeUnsignedShort.INSTANCE)) {
            NativeImg<UnsignedShortType, ShortArray> natIn =
                    ImgLib2Wrap.wrapShort(chnlIn.voxels().asShort());
            process(natIn, natOut, (float) scaleFactor);
        } else {
            throw new CreateException("Input type must be unsigned byte or short");
        }

        // convert to our output from the float
        ChannelConverter<?> converter =
                outputShort
                        ? new ChannelConverterToUnsignedShort()
                        : new ChannelConverterToUnsignedByte();
        return converter.convert(chnlIntermediate, ConversionPolicy.CHANGE_EXISTING_CHANNEL);
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
