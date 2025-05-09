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

package org.anchoranalysis.plugin.image.bean.object.provider;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.image.bean.provider.ChannelProvider;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProviderUnary;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.voxel.object.ObjectCollection;

/** Base class for {@link ObjectCollectionProviderUnary} that also requires a {@link Channel}. */
public abstract class UnaryWithChannelBase extends ObjectCollectionProviderUnary {

    // START BEAN PROPERTIES
    /** Provider for the channel to be used in object creation. */
    @BeanField @Getter @Setter private ChannelProvider channel;

    // END BEAN PROPERTIES

    @Override
    public ObjectCollection createFromObjects(ObjectCollection objectsSource)
            throws ProvisionFailedException {

        return createFromObjects(objectsSource, channel.get());
    }

    /**
     * Creates objects from a source collection and a channel.
     *
     * @param objectsSource the source object collection
     * @param channelSource the source channel
     * @return the newly created object collection
     * @throws ProvisionFailedException if object creation fails
     */
    protected abstract ObjectCollection createFromObjects(
            ObjectCollection objectsSource, Channel channelSource) throws ProvisionFailedException;
}
