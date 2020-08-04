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

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChannelProvider;
import org.anchoranalysis.image.bean.provider.ChnlProviderOne;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.datatype.CombineTypes;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;

/**
 * Creates a new channel which is a merged version of two input channels according to rules.
 *
 * <ul>
 *   <li>If the pixel in
 *       <pre>chnl</pre>
 *       is non-zero, then the corresponding output is
 *       <pre>chnl</pre>
 *   <li>If the pixel in
 *       <pre>chnl</pre>
 *       is zero, then the corresponding output is
 *       <pre>chnlIfPixelZero</pre>
 * </ul>
 *
 * <p>The two channels must be the same size.
 *
 * <p>Neither channel's input is changed. The operation is <b>immutable</b>.
 *
 * @author Owen Feehan
 */
public class ChnlProviderIfPixelZero extends ChnlProviderOne {

    // START BEAN PROPERTIES
    /**
     * If a pixel is zero in the input-channel, the output is formed from the corresponding pixel in
     * this channel instead
     */
    @BeanField @Getter @Setter private ChannelProvider chnlIfPixelZero;
    // END BEAN PROPERTIES

    @Override
    public Channel createFromChnl(Channel chnl) throws CreateException {

        Channel ifZero = DimChecker.createSameSize(chnlIfPixelZero, "chnlIfPixelZero", chnl);

        VoxelDataType combinedType =
                CombineTypes.combineTypes(chnl.getVoxelDataType(), ifZero.getVoxelDataType());

        double multFact = (double) combinedType.maxValue() / chnl.getVoxelDataType().maxValue();
        return mergeViaZeroCheck(chnl, ifZero, combinedType, multFact);
    }

    /**
     * Creates a new channel which is a merged version of two input channels according to rules.
     *
     * <ul>
     *   <li>If the pixel in {@code chnl} is non-zero, then the corresponding output is {@code chnl
     *       * multFactorIfNonZero}
     *   <li>If the pixel in {@code chnl} is zero, then the corresponding output is {@code
     *       chnlIfPixelZero}
     * </ul>
     *
     * <p>Assumes the two channels are of the same size, but does not check.
     *
     * <p>Neither channel's input is changed. The operation is <i>immutable</i>.
     *
     * @param channel the channel that is checked to be zero/non-zero, and whose pixels form the
     *     output (maybe multipled) if non-zero
     * @param channelIfPixelZero the channel that forms the output if {@code chnl} is zero
     * @param combinedType the type to use for the newly created channel
     * @param multFactorIfNonZero the multiplication factor to apply on non-zero pixels
     * @return a newly created merged channel according to the above rules
     */
    public static Channel mergeViaZeroCheck(
            Channel channel,
            Channel channelIfPixelZero,
            VoxelDataType combinedType,
            double multFactorIfNonZero) {

        Channel channelOut =
                ChannelFactory.instance()
                        .createEmptyInitialised(channel.getDimensions(), combinedType);

        // We know these are all the same types from the logic above, so we can safetly cast
        processVoxelBox(
                channelOut.voxels(),
                channel.voxels(),
                channelIfPixelZero.voxels(),
                multFactorIfNonZero);

        return channelOut;
    }

    private static void processVoxelBox(
            VoxelBoxWrapper vbOut,
            VoxelBoxWrapper vbIn,
            VoxelBoxWrapper vbIfZero,
            double multFactorIfNonZero) {

        int volumeXY = vbIn.any().extent().getVolumeXY();

        for (int z = 0; z < vbOut.any().extent().getZ(); z++) {

            VoxelBuffer<?> in1 = vbIn.any().getPixelsForPlane(z);
            VoxelBuffer<?> in2 = vbIfZero.any().getPixelsForPlane(z);
            VoxelBuffer<?> out = vbOut.any().getPixelsForPlane(z);

            for (int offset = 0; offset < volumeXY; offset++) {

                int b1 = in1.getInt(offset);

                if (b1 != 0) {
                    out.putInt(offset, (int) (b1 * multFactorIfNonZero));
                } else {
                    int b2 = in2.getInt(offset);
                    out.putInt(offset, b2);
                }
            }
        }
    }
}
