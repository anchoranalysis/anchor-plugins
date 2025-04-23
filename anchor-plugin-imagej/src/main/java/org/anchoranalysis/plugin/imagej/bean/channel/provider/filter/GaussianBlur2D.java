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

package org.anchoranalysis.plugin.imagej.bean.channel.provider.filter;

import ij.plugin.filter.GaussianBlur;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.image.bean.provider.ChannelProviderUnary;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.io.imagej.convert.ImageJConversionException;
import org.anchoranalysis.plugin.imagej.channel.provider.FilterHelper;

/** Applies a 2D Gaussian blur filter to each slice of a {@link Channel} independently. */
public class GaussianBlur2D extends ChannelProviderUnary {

    // START BEAN PROPERTIES
    /** The standard deviation of the Gaussian distribution used for blurring. */
    @BeanField @Positive @Getter @Setter private double sigma = 3;

    // END BEAN PROPERTIES

    @Override
    public Channel createFromChannel(Channel channel) throws ProvisionFailedException {
        try {
            return blur(channel);
        } catch (ImageJConversionException e) {
            throw new ProvisionFailedException(e);
        }
    }

    /**
     * Applies the Gaussian blur to each slice of the channel.
     *
     * @param channel the {@link Channel} to blur
     * @return the blurred channel
     * @throws ImageJConversionException if there's an error converting the channel to ImageJ format
     */
    @SuppressWarnings("deprecation")
    private Channel blur(Channel channel) throws ImageJConversionException {
        GaussianBlur blur = new GaussianBlur();
        FilterHelper.processEachSlice(channel, processor -> blur.blur(processor, sigma)); // NOSONAR
        return channel;
    }
}
