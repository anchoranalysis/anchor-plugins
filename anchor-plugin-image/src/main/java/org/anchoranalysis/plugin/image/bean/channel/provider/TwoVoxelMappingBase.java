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

package org.anchoranalysis.plugin.image.bean.channel.provider;

import java.util.function.IntBinaryOperator;
import lombok.AllArgsConstructor;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.image.bean.provider.ChannelProviderBinary;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.channel.factory.ChannelFactory;
import org.anchoranalysis.image.voxel.datatype.UnsignedByteVoxelType;
import org.anchoranalysis.image.voxel.iterator.IterateVoxelsAll;

/**
 * Takes the two channels and creates a NEW third channel whose pixels are a function of the two
 * channels
 *
 * <p>Both the two input channels and the output channel are identically-sized.
 *
 * @author Owen Feehan
 */
@AllArgsConstructor
public abstract class TwoVoxelMappingBase extends ChannelProviderBinary {

    /** How to form an output voxel from the respective two input voxels. */
    private final IntBinaryOperator operation;

    @Override
    protected Channel process(Channel channel1, Channel channel2) throws ProvisionFailedException {

        Channel channelOut =
                ChannelFactory.instance()
                        .create(channel1.dimensions(), UnsignedByteVoxelType.INSTANCE);

        IterateVoxelsAll.binaryOperation(
                channel1.voxels().asByte(),
                channel2.voxels().asByte(),
                channelOut.voxels().asByte(),
                operation);

        return channelOut;
    }
}
