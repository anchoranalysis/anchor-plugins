/*-
 * #%L
 * anchor-plugin-ij
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

package org.anchoranalysis.plugin.ij.bean.channel.provider.filter;

import ij.plugin.filter.GaussianBlur;
import ij.process.ImageProcessor;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChannelProviderUnary;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.convert.IJWrap;
import org.anchoranalysis.image.voxel.VoxelsWrapper;

public class GaussianBlur2D extends ChannelProviderUnary {

    // START BEAN PROPERTIES
    @BeanField @Positive @Getter @Setter private double sigma = 3;
    // END BEAN PROPERTIES

    @SuppressWarnings("deprecation")
    private Channel blur(Channel channel) {

        GaussianBlur gb = new GaussianBlur();

        VoxelsWrapper voxels = channel.voxels();

        // Are we missing a Z slice?
        for (int z = 0; z < channel.dimensions().z(); z++) {
            ImageProcessor processor = IJWrap.imageProcessor(voxels, z);
            gb.blur(processor, sigma); // NOSONAR
        }

        return channel;
    }

    @Override
    public Channel createFromChannel(Channel channel) throws CreateException {
        return blur(channel);
    }
}