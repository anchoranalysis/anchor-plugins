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

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.AnchorCheckedException;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitialization;
import org.anchoranalysis.image.bean.provider.ChannelProvider;
import org.anchoranalysis.image.bean.provider.DimensionsProvider;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.stack.Stack;

/**
 * Creates image-dimensions by referencing them from a ChannelProvider
 *
 * <p>One of either channelProvider or id must be set, but not both
 *
 * <p>id will look for a Channel or a Stack in that order
 */
public class FromChannel extends DimensionsProvider {

    // START BEAN PROPERTIES
    @BeanField @AllowEmpty @Getter @Setter private String id = "";

    @BeanField @OptionalBean @Getter @Setter private ChannelProvider channel;
    // END BEAN PROPERTIES

    @Override
    public void onInitialization(ImageInitialization initialization) throws InitializeException {
        super.onInitialization(initialization);
        if (id.isEmpty() && channel == null) {
            throw new InitializeException("One of either channelProvider or id must be set");
        }
        if (!id.isEmpty() && channel != null) {
            throw new InitializeException("Only one -not both- of channelProvider and id should be set");
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

    private Channel selectChannelForId(String id) throws ProvisionFailedException {

        try {
            return OptionalUtilities.orFlat(
                            () -> getInitialization().channels().getOptional(id),
                            () ->
                                    getInitialization()
                                            .stacks()
                                            .getOptional(id)
                                            .map(FromChannel::firstChannel))
                    .orElseThrow(
                            () ->
                                    new ProvisionFailedException(
                                            String.format(
                                                    "Failed to find either a channel or stack with id `%s`",
                                                    id)));

        } catch (AnchorCheckedException e) {
            throw new ProvisionFailedException(
                    String.format("A error occurred while retrieving channel `%s`", id), e);
        }
    }

    private static Channel firstChannel(Stack stack) {
        return stack.getChannel(0);
    }
}
