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
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.identifier.provider.NamedProviderGetException;
import org.anchoranalysis.image.bean.provider.ChannelProvider;
import org.anchoranalysis.image.core.channel.Channel;

/**
 * Provides a channel by referencing it from a a set of named-channels.
 *
 * <p>This class extends {@link ChannelProvider} to retrieve a channel by its ID from a named
 * provider. It also offers an option to duplicate the retrieved channel.
 */
@NoArgsConstructor
public class Reference extends ChannelProvider {

    // START BEAN PROPERTIES
    /** The ID of the channel to retrieve from the named-provider. */
    @BeanField @Getter @Setter private String id = "";

    /**
     * If true, the channel is duplicated after it is retrieved, to prevent overwriting existing
     * data.
     *
     * <p>This is a shortcut to avoid embedding beans in a ChannelProviderDuplicate.
     */
    @BeanField @Getter @Setter private boolean duplicate = false;

    // END BEAN PROPERTIES

    /** The cached channel retrieved from the named provider. */
    private Channel channel;

    /**
     * Creates a new instance with a specified channel ID.
     *
     * @param id the ID of the channel to retrieve
     */
    public Reference(String id) {
        super();
        this.id = id;
    }

    @Override
    public Channel get() throws ProvisionFailedException {
        if (channel == null) {
            channel = getMaybeDuplicate();
        }
        return channel;
    }

    /**
     * Retrieves the channel from the named provider and optionally duplicates it.
     *
     * @return the retrieved {@link Channel}, possibly duplicated
     * @throws ProvisionFailedException if the channel cannot be retrieved or duplicated
     */
    private Channel getMaybeDuplicate() throws ProvisionFailedException {
        try {
            Channel existing = getInitialization().channels().getException(id);

            if (duplicate) {
                return existing.duplicate();
            } else {
                return existing;
            }
        } catch (NamedProviderGetException | InitializeException e) {
            throw new ProvisionFailedException(e);
        }
    }
}
