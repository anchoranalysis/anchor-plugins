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

package org.anchoranalysis.plugin.image.bean.channel.provider.intensity;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChannelProvider;
import org.anchoranalysis.image.bean.provider.ChannelProviderUnary;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.voxel.VoxelsWrapper;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.datatype.CombineTypes;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;
import org.anchoranalysis.plugin.image.channel.DimensionsChecker;

/**
 * Creates a new channel which is a merged version of two input channels according to rules.
 *
 * <ul>
 *   <li>If the voxel in
 *       <pre>channel</pre>
 *       is non-zero, then the corresponding output is
 *       <pre>channel</pre>
 *   <li>If the voxel in
 *       <pre>channel</pre>
 *       is zero, then the corresponding output is
 *       <pre>channelIfVoxelZero</pre>
 * </ul>
 *
 * <p>The two channels must be the same size.
 *
 * <p>Neither channel's input is changed. The operation is <b>immutable</b>.
 *
 * @author Owen Feehan
 */
public class IfVoxelZero extends ChannelProviderUnary {

    // START BEAN PROPERTIES
    /**
     * If a pixel is zero in the input-channel, the output is formed from the corresponding pixel in
     * this channel instead
     */
    @BeanField @Getter @Setter private ChannelProvider channelIfVoxelZero;
    // END BEAN PROPERTIES

    @Override
    public Channel createFromChannel(Channel channel) throws CreateException {

        Channel ifZero = DimensionsChecker.createSameSize(channelIfVoxelZero, "channelIfVoxelZero", channel);

        VoxelDataType combinedType =
                CombineTypes.combineTypes(channel.getVoxelDataType(), ifZero.getVoxelDataType());

        double multFact = (double) combinedType.maxValue() / channel.getVoxelDataType().maxValue();
        return mergeViaZeroCheck(channel, ifZero, combinedType, multFact);
    }

    /**
     * Creates a new channel which is a merged version of two input channels according to rules.
     *
     * <ul>
     *   <li>If the pixel in {@code channel} is non-zero, then the corresponding output is {@code channel
     *       * multFactorIfNonZero}
     *   <li>If the pixel in {@code channel} is zero, then the corresponding output is {@code
     *       channelIfPixelZero}
     * </ul>
     *
     * <p>Assumes the two channels are of the same size, but does not check.
     *
     * <p>Neither channel's input is changed. The operation is <i>immutable</i>.
     *
     * @param channel the channel that is checked to be zero/non-zero, and whose pixels form the
     *     output (maybe multipled) if non-zero
     * @param channelIfPixelZero the channel that forms the output if {@code channel} is zero
     * @param combinedType the type to use for the newly created channel
     * @param multFactorIfNonZero the multiplication factor to apply on non-zero pixels
     * @return a newly created merged channel according to the above rules
     */
    public static Channel mergeViaZeroCheck(
            Channel channel,
            Channel channelIfPixelZero,
            VoxelDataType combinedType,
            double multFactorIfNonZero) {

        Channel channelOut = ChannelFactory.instance().create(channel.dimensions(), combinedType);

        // We know these are all the same types from the logic above, so we can safetly cast
        processVoxels(
                channelOut.voxels(),
                channel.voxels(),
                channelIfPixelZero.voxels(),
                multFactorIfNonZero);

        return channelOut;
    }

    private static void processVoxels(
            VoxelsWrapper voxelsOut,
            VoxelsWrapper voxelsIn,
            VoxelsWrapper voxelsIfZero,
            double multFactorIfNonZero) {

        int volumeXY = voxelsIn.extent().volumeXY();

        for (int z = 0; z < voxelsOut.extent().z(); z++) {

            VoxelBuffer<?> in1 = voxelsIn.slice(z);
            VoxelBuffer<?> in2 = voxelsIfZero.slice(z);
            VoxelBuffer<?> out = voxelsOut.slice(z);

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
