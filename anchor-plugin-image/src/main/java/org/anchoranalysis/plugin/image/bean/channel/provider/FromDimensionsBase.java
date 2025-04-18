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

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.image.bean.provider.ChannelProvider;
import org.anchoranalysis.image.bean.provider.DimensionsProvider;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.plugin.image.bean.dimensions.provider.GuessDimensions;

/**
 * An abstract base class for creating a channel from specified dimensions.
 *
 * <p>This class extends {@link ChannelProvider} to provide a framework for creating channels
 * based on given dimensions. Subclasses must implement the {@code createFromDimensions} method.</p>
 */
public abstract class FromDimensionsBase extends ChannelProvider {

    // START BEAN PROPERTIES
    /**
     * The provider for the dimensions of the channel to be created.
     *
     * <p>Defaults to {@link GuessDimensions} if not explicitly set.</p>
     */
    @BeanField @Getter @Setter private DimensionsProvider dimensions = new GuessDimensions();
    // END BEAN PROPERTIES

    @Override
    public Channel get() throws ProvisionFailedException {
        return createFromDimensions(dimensions.get());
    }

    /**
     * Creates a channel from the given dimensions.
     *
     * @param dimensions the {@link Dimensions} to use for creating the channel
     * @return a new {@link Channel} with the specified dimensions
     * @throws ProvisionFailedException if the channel creation fails
     */
    protected abstract Channel createFromDimensions(Dimensions dimensions)
            throws ProvisionFailedException;
}