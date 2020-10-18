/*-
 * #%L
 * anchor-plugin-image-task
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

package org.anchoranalysis.plugin.image.task.bean.format.convertstyle;

import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.stack.NamedStacks;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.plugin.image.task.channel.ChannelGetterForTimepoint;

/**
 * Converts each channel independently and creates a single-channeled stack from the conversion.
 * 
 * <p>An identical channel-name is used to identify it among the outputted stacks.
 * 
 * @author Owen Feehan
 *
 */
public class IndependentChannels extends ChannelConvertStyle {

    // START BEAN PROPERTIES
    /** Iff true and we cannot find a channel in the file, we ignore it and carry on */
    @BeanField @Getter @Setter private boolean ignoreMissingChannel = true;
    // END BEAN PROPERTIES

    @Override
    public NamedStacks convert(
            Set<String> channelNames,
            ChannelGetterForTimepoint channelGetter,
            Logger logger)
            throws OperationFailedException {

        NamedStacks out = new NamedStacks();
        
        for (String key : channelNames) {
            convertChannel(key, channelGetter, logger).ifPresent( stack ->
                out.add(key,  stack)
            );
        }
        
        return out;
    }
    
    private Optional<Stack> convertChannel(String key, ChannelGetterForTimepoint channelGetter, Logger logger) throws OperationFailedException {
        try {
            Channel channel = channelGetter.getChannel(key);
            return Optional.of( new Stack(channel) );
            
        } catch (GetOperationFailedException e) {
            if (ignoreMissingChannel) {
                logger.messageLogger().logFormatted("Cannot open channel '%s'. Ignoring.", key);
                return Optional.empty();
            } else {
                throw new OperationFailedException(String.format("Cannot open channel '%s'.", key), e);
            }
        } 
    }
}
