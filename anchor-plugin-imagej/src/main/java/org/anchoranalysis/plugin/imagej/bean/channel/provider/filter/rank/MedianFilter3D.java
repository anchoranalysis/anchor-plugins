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

package org.anchoranalysis.plugin.imagej.bean.channel.provider.filter.rank;

import ij.ImagePlus;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.image.bean.provider.ChannelProviderUnary;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.io.imagej.convert.ConvertFromImagePlus;
import org.anchoranalysis.io.imagej.convert.ConvertToImagePlus;
import org.anchoranalysis.io.imagej.convert.ImageJConversionException;

/** Applies a 3D median filter to a channel using a hybrid 3D median filter ImageJ plugin. */
public class MedianFilter3D extends ChannelProviderUnary {

    @Override
    public Channel createFromChannel(Channel channel) throws ProvisionFailedException {
        try {
            ImagePlus image = ConvertToImagePlus.from(channel);

            ImagePlus filtered = applyFilter(image);

            return ConvertFromImagePlus.toChannel(filtered, channel.resolution());
        } catch (ImageJConversionException e) {
            throw new ProvisionFailedException(e);
        }
    }

    /**
     * Applies the 3D median filter to the given ImagePlus.
     *
     * @param image the {@link ImagePlus} to filter
     * @return the filtered {@link ImagePlus}
     */
    private ImagePlus applyFilter(ImagePlus image) {
        Hybrid_3D_Median_Filter plugin = new Hybrid_3D_Median_Filter();
        plugin.setup("", image);

        return plugin.Hybrid3dMedianizer(image);
    }
}
