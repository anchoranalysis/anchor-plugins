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

package org.anchoranalysis.plugin.image.bean.channel.provider.convert;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.OptionalProviderFactory;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.image.bean.channel.ConvertChannelToWithHistogram;
import org.anchoranalysis.image.bean.provider.HistogramProvider;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.channel.convert.attached.ChannelConverterAttached;
import org.anchoranalysis.image.voxel.statistics.HistogramFactory;
import org.anchoranalysis.math.histogram.Histogram;

/**
 * Converts a channel to a different voxel data-type by applying a converter with an associated
 * histogram.
 *
 * <p>The histogram can be either provided, or it will be derived from the image anew.
 *
 * @author Owen Feehan
 * @param <T> the buffer-type to convert the channel to
 */
public class ConvertWithHistogram<T> extends ConvertBase {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ConvertChannelToWithHistogram<T> convert;

    /**
     * If set, a histogram used in conversion to describe input intensities (across perhaps multiple
     * images). If unset, such a histogram is calculated from the current image.
     */
    @BeanField @OptionalBean @Getter @Setter private HistogramProvider histogram;

    // END BEAN PROPERTIES

    @Override
    public Channel createFromChannel(final Channel channel) throws ProvisionFailedException {

        ChannelConverterAttached<Histogram, T> converter = convert.createConverter();

        try {
            converter.attachObject(createHistogram(channel));
        } catch (OperationFailedException e) {
            throw new ProvisionFailedException(e);
        }

        return converter.convert(channel, createPolicy());
    }

    private Histogram createHistogram(Channel channel) throws ProvisionFailedException {
        return OptionalUtilities.orElseGet(
                OptionalProviderFactory.create(histogram),
                () -> HistogramFactory.createFrom(channel.voxels()));
    }
}
