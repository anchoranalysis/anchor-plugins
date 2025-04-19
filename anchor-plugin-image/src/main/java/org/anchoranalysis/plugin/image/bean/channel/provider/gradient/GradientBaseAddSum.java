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

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.channel.factory.ChannelFactory;
import org.anchoranalysis.image.voxel.datatype.FloatVoxelType;

/**
 * Like {@link GradientBase} but allows an arbitrary constant to be added to voxels storing the
 * gradient.
 *
 * <p>An out-of-bounds strategy of <i>mirror</i> is used for calculating the gradient for voxels
 * lying at the boundary.
 *
 * @author Owen Feehan
 */
public abstract class GradientBaseAddSum extends GradientBase {

    // START BEAN FIELDS
    /**
     * Added to all gradients (so we can store negative gradients).
     *
     * <p>Default value is 0.
     */
    @BeanField @Getter @Setter private int addSum = 0;
    // END BEAN FIELDS

    @Override
    public Channel createFromChannel(Channel channelIn) throws ProvisionFailedException {

        // The gradient is calculated on a float
        Channel channelIntermediate =
                ChannelFactory.instance().create(channelIn.dimensions(), FloatVoxelType.INSTANCE);

        GradientCalculator calculator =
                new GradientCalculator(createAxisArray(), (float) getScaleFactor(), addSum);
        calculator.gradient(channelIn.voxels(), channelIntermediate.voxels().asFloat());

        return convertToOutputType(channelIntermediate);
    }

    /**
     * Creates an array of booleans indicating which axes to calculate the gradient for.
     *
     * @return a boolean array where true indicates the gradient should be calculated for that axis
     * @throws ProvisionFailedException if there's an error creating the axis array
     */
    protected abstract boolean[] createAxisArray() throws ProvisionFailedException;
}
