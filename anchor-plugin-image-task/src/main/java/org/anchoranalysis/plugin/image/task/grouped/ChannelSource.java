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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.identifier.provider.NamedProviderGetException;
import org.anchoranalysis.image.bean.nonbean.ConsistentChannelChecker;
import org.anchoranalysis.image.bean.spatial.SizeXY;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.core.stack.named.NamedStacks;
import org.anchoranalysis.image.voxel.resizer.VoxelsResizer;

/**
 * Extracts a set of {@link Channel]s from a {@link NamedStacks}, optionally resizing.
 *
 * <p>Checks may be applied to make sure all channels have the same-type
 */
@RequiredArgsConstructor
public class ChannelSource {

    // START REQUIRED ARGUMENTS
	/** 
	 * A named set of {@link Stack}s from which {@link Channel}s may be extracted.
	 */
    @Getter private final NamedStacks stackStore;

    /** How to check that the {@link Channel}s have consistent voxel data-type. */
    private final ConsistentChannelChecker channelChecker;

    /** If set, resizes all extracted channels in the XY dimensions. */
    private final Optional<SizeXY> resizeTo;

    /** How to resize the {@link Channel}s. */
    private final VoxelsResizer resizer;
    // END REQUIRED ARGUMENTS

    /**
     * Extracts a {@link Channel} from a particular {@link Stack} in {@code stackStore}.
     * 
     * <p>This {@link Stack} must be single-channeled.
     * 
     * @param stackName the name of the {@link Stack} which contains the channel.
     * @param checkType if true, a call occurs to {@code channelChecker} to ensure all {@link Channel}s have consistent voxel data-type.
     * @return the extracted {@link Channel}, reused from {@code stackStore}.
     * @throws OperationFailedException if the {@link Stack} is not single-channeled, or if non consistent voxel data-type occurs.
     */
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
        Channel channel = stack.getChannel(index);

        if (checkType) {
            channelChecker.checkChannelType(channel);
        }

        return maybeResize(channel);
    }

    private Channel maybeResize(Channel channel) {
        if (resizeTo.isPresent()) {
            return channel.resizeXY(resizeTo.get().getWidth(), resizeTo.get().getHeight(), resizer);
        } else {
            return channel;
        }
    }
}
