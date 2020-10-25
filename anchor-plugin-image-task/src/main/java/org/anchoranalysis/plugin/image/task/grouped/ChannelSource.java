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

package org.anchoranalysis.plugin.image.task.grouped;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.identifier.provider.NamedProviderGetException;
import org.anchoranalysis.core.index.SetOperationFailedException;
import org.anchoranalysis.image.bean.spatial.SizeXY;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.core.stack.named.NamedStacks;

/**
 * Source of channels for aggregating.
 *
 * <p>Checks may be applied to make sure all channels have the same-type
 */
@RequiredArgsConstructor
public class ChannelSource {

    // START REQUIRED ARGUMENTS
    private final NamedStacks stackStore;
    private final ConsistentChannelChecker channelChecker;
    // END REQUIRED ARGUMENTS

    /** Optionally resizes all extracted channels in XY */
    private final Optional<SizeXY> resizeTo;

    public Channel extractChannel(String stackName, boolean checkType)
            throws OperationFailedException {

        try {
            // We make a single histogram
            Stack stack = stackStore.getException(stackName);

            if (stack.getNumberChannels() > 1) {
                throw new OperationFailedException("Each stack may only have a single channel");
            }

            return extractChannel(stack, checkType, 0);
        } catch (NamedProviderGetException e) {
            throw new OperationFailedException(
                    String.format("Cannot extract a single channel from stack %s", stackName),
                    e.summarize());
        }
    }

    public Channel extractChannel(String stackName, boolean checkType, int index)
            throws OperationFailedException {

        try {
            // We make a single histogram
            Stack stack = stackStore.getException(stackName);
            return extractChannel(stack, checkType, index);

        } catch (NamedProviderGetException e) {
            throw new OperationFailedException(
                    String.format("Cannot extract channel %d from stack %s", index, stackName),
                    e.summarize());
        }
    }

    public Channel extractChannel(Stack stack, boolean checkType, int index)
            throws OperationFailedException {
        try {
            Channel channel = stack.getChannel(index);

            if (checkType) {
                channelChecker.checkChannelType(channel.getVoxelDataType());
            }

            return maybeResize(channel);
        } catch (SetOperationFailedException e) {
            throw new OperationFailedException(
                    String.format("Cannot extract channel %d from stack", index), e);
        }
    }

    private Channel maybeResize(Channel channel) {
        if (resizeTo.isPresent()) {
            return channel.resizeXY(resizeTo.get().getWidth(), resizeTo.get().getHeight());
        } else {
            return channel;
        }
    }

    public NamedStacks getStackStore() {
        return stackStore;
    }
}
