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
import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.exception.BeanMisconfiguredException;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.identifier.provider.NamedProviderGetException;
import org.anchoranalysis.image.bean.provider.ChannelProvider;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.stack.ProviderAsStack;
import org.anchoranalysis.image.core.stack.Stack;

/** 
 * Extracts a channel from a provider of a stack.
 *
 * <p>This class extends {@link ChannelProvider} to provide a single channel from a stack,
 * which can be specified either by a provider ID or directly as a {@link ProviderAsStack}.</p>
 */
@NoArgsConstructor
public class FromStack extends ChannelProvider {

    // START BEAN PROPERTIES
    /** 
     * The index of the channel to extract from the stack.
     */
    @BeanField @Getter @Setter private int channelIndex = 0;

    /**
     * Specifies the ID of an existing stack. Either this must be specified or else {@code stack}
     * must be specified.
     */
    @BeanField @AllowEmpty @Getter @Setter private String stackProviderID;

    /**
     * Provides a stack. Either this must be specified or else {@code stackProviderID} must be
     * specified.
     */
    @BeanField @OptionalBean @Getter @Setter private ProviderAsStack stack;
    // END BEAN PROPERTIES

    /**
     * Creates a new instance with a specified stack provider.
     *
     * @param stack the {@link ProviderAsStack} to use for providing the stack
     */
    public FromStack(ProviderAsStack stack) {
        this.stack = stack;
    }

    @Override
    public void checkMisconfigured(BeanInstanceMap defaultInstances)
            throws BeanMisconfiguredException {
        super.checkMisconfigured(defaultInstances);
        if (stackProviderID.isEmpty() && stack == null) {
            throw new BeanMisconfiguredException(
                    "One of either stackProviderID or stack must be specified");
        }
        if (!stackProviderID.isEmpty() && stack != null) {
            throw new BeanMisconfiguredException(
                    "Only one of either stackProviderID or stack must be specified, but not both.");
        }
    }

    /** The cached channel extracted from the stack. */
    private Channel channel;

    @Override
    public Channel get() throws ProvisionFailedException {

        if (channel == null) {
            channel = stackFromSource().getChannel(channelIndex);
            if (channel == null) {
                throw new ProvisionFailedException(
                        String.format("channel %d cannot be found in stack", channelIndex));
            }
        }

        return channel;
    }

    /**
     * Retrieves the stack from either the specified provider or the stack provider ID.
     *
     * @return the {@link Stack} from which to extract the channel
     * @throws ProvisionFailedException if the stack cannot be retrieved
     */
    private Stack stackFromSource() throws ProvisionFailedException {
        if (stack != null) {
            return stack.getAsStack();
        } else {
            try {
                return getInitialization().stacks().getException(stackProviderID);
            } catch (NamedProviderGetException e) {
                throw new ProvisionFailedException(e.summarize());
            } catch (InitializeException e) {
                throw new ProvisionFailedException(e);
            }
        }
    }
}