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

package org.anchoranalysis.plugin.imagej.bean.channel.provider;

import lombok.Getter;
import lombok.Setter;
import java.util.Optional;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChannelProviderUnary;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.dimensions.Resolution;
import org.anchoranalysis.image.dimensions.UnitConverter;

/** A {@link ChannelProviderUnary} with a 'radius' parameter */
public abstract class WithRadiusBase extends ChannelProviderUnary {

    // START BEAN PROPERTIES
    @BeanField @Positive @Getter @Setter private double radius = 2;

    @BeanField @Getter @Setter
    private boolean radiusInMeters = false; // Treats radius if it's microns
    // END BEAN PROPERTIES

    @Override
    public Channel createFromChannel(Channel channel) throws CreateException {
        return createFromChannel(channel, radiusInVoxels(channel.resolution().map(Resolution::unitConvert)));
    }

    protected abstract Channel createFromChannel(Channel channel, int radius)
            throws CreateException;

    private int radiusInVoxels(Optional<UnitConverter> converter) throws CreateException {
        if (radiusInMeters) {
            if (converter.isPresent()) {
                // Then we reconcile our sigma in microns against the Pixel Size XY (Z is taken care of
                // later)
                return (int) Math.round(converter.get().fromPhysicalDistance(radius));
            } else {
                throw new CreateException("Radius is specified in meters but no image-resolution information is available");
            }
        } else {
            return (int) radius;
        }
    }
}
