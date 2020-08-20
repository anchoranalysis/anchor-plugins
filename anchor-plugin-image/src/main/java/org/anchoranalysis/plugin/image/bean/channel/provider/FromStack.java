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
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.image.bean.provider.ChannelProvider;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.provider.ProviderAsStack;
import org.anchoranalysis.image.stack.Stack;

/** Extracts a channel from a provider of a stack */
@NoArgsConstructor
public class FromStack extends ChannelProvider {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private int channelIndex = 0;

    /** Specifies the ID of an existing stack. Either this must be specified or else {@code stack} must be specified. */
    @BeanField @AllowEmpty @Getter @Setter private String stackProviderID;

    /** Provides a stack. Either this must be specified or else {@code stackProviderID} must be specified. */
    @BeanField @OptionalBean @Getter @Setter private ProviderAsStack stack;
    // END BEAN PROPERTIES

    public FromStack(ProviderAsStack stack) {
        this.stack = stack;
    }
    
    @Override
    public void checkMisconfigured(BeanInstanceMap defaultInstances)
            throws BeanMisconfiguredException {
        super.checkMisconfigured(defaultInstances);
        if (stackProviderID.isEmpty() && stack==null) {
            throw new BeanMisconfiguredException("One of either stackProviderID or stack must be specified");
        }
        if (!stackProviderID.isEmpty() && stack!=null) {
            throw new BeanMisconfiguredException("Only one of either stackProviderID or stack must be specified, but not both.");
        }
    }
    
    private Channel channel;

    @Override
    public Channel create() throws CreateException {

        if (channel == null) {
            channel = stackFromSource().getChannel(channelIndex);
            if (channel == null) {
                throw new CreateException(String.format("channel %d cannot be found in stack", channelIndex));
            }
        }

        return channel;
    }
    
    private Stack stackFromSource() throws CreateException {
        if (stack!=null) {
            return stack.createAsStack();
        } else {
            try {
                return getInitializationParameters()
                        .stacks()
                        .getException(stackProviderID);
            } catch (NamedProviderGetException e) {
                throw new CreateException(e.summarize());
            }
        }
    }
}
