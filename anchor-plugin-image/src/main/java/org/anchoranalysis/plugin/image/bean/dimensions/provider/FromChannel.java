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

package org.anchoranalysis.plugin.image.bean.dimensions.provider;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.AnchorCheckedException;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.core.identifier.provider.NamedProviderGetException;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitialization;
import org.anchoranalysis.image.bean.provider.ChannelProvider;
import org.anchoranalysis.image.bean.provider.DimensionsProvider;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.stack.Stack;

/**
 * Creates image-dimensions by referencing them from a {@link ChannelProvider}.
 *
 * <p>One of either {@code channel} or {@code id} must be set, but not both.
 *
 * <p>It will look, in order of preference, for respectively a {@link Channel} and then a {@link
 * Stack}.
 */
public class FromChannel extends DimensionsProvider {

    // START BEAN PROPERTIES
    /** An shared-objects identifier for a {@link ChannelProvider} to use for dimensions. */
    @BeanField @AllowEmpty @Getter @Setter private String id = "";

    /** The {@link ChannelProvider} to use for dimensions. */
    @BeanField @OptionalBean @Getter @Setter private ChannelProvider channel;
    // END BEAN PROPERTIES

    @Override
    public void onInitialization(ImageInitialization initialization) throws InitializeException {
        super.onInitialization(initialization);
        if (id.isEmpty() && channel == null) {
            throw new InitializeException("One of either channelProvider or id must be set");
        }
        if (!id.isEmpty() && channel != null) {
            throw new InitializeException(
                    "Only one -not both- of channelProvider and id should be set");
        }
    }

    @Override
    public Dimensions get() throws ProvisionFailedException {
        return selectChannel().dimensions();
    }

    private Channel selectChannel() throws ProvisionFailedException {

        if (!id.isEmpty()) {
            return selectChannelForId(id);
        }

        return channel.get();
    }

    private Channel selectChannelForId(String identifier) throws ProvisionFailedException {

        try {
            Optional<Channel> combined =
                    OptionalUtilities.orFlatSupplier(
                            () -> channelDirectly(identifier),
                            () -> channelFromStacks(identifier));
            return combined.orElseThrow( () -> provisionException(identifier) );

        } catch (AnchorCheckedException e) {
            throw new ProvisionFailedException(
                    String.format("A error occurred while retrieving channel `%s`", identifier), e);
        }
    }
    
    /** Retrieves a {@link Channel} from a collection of named {@link Channel}s. */
    private Optional<Channel> channelDirectly(String identifier) throws NamedProviderGetException, InitializeException {
        return getInitialization().channels().getOptional(identifier);
    }
    
    /** Retrieves a {@link Channel} from a collection of named {@link Stack}s. */
    private Optional<Channel> channelFromStacks(String identifier) throws NamedProviderGetException, InitializeException {
        return getInitialization().stacks().getOptional(identifier).map( stack -> stack.getChannel(0) );
    }
    
    /** The exception thrown if a channel cannot be found to match {@code identifier}. */
    private static ProvisionFailedException provisionException(String identifier) {
        return new ProvisionFailedException(
                String.format(
                        "Failed to find either a channel or stack with id `%s`",
                        identifier));
    }
}
