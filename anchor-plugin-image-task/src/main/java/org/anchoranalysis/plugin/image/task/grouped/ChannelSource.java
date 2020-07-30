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
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.index.SetOperationFailedException;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.image.bean.size.SizeXY;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.stack.NamedImgStackCollection;
import org.anchoranalysis.image.stack.Stack;

/**
 * Source of channels for aggregating.
 *
 * <p>Checks may be applied to make sure all chnls have the same-type
 */
public class ChannelSource {

    private final NamedImgStackCollection stackStore;
    private final ConsistentChannelChecker chnlChecker;
    private final Optional<SizeXY> resizeTo;

    /**
     * Constructor
     *
     * @param stackStore
     * @param chnlChecker
     * @param resizeTo optionally resizes all extracted channels in XY
     */
    public ChannelSource(
            NamedImgStackCollection stackStore,
            ConsistentChannelChecker chnlChecker,
            Optional<SizeXY> resizeTo) {
        super();
        this.stackStore = stackStore;
        this.chnlChecker = chnlChecker;
        this.resizeTo = resizeTo;
    }

    public Channel extractChnl(String stackName, boolean checkType)
            throws OperationFailedException {

        try {
            // We make a single histogram
            Stack stack = stackStore.getException(stackName);

            if (stack.getNumberChannels() > 1) {
                throw new OperationFailedException("Each stack may only have a single channel");
            }

            return extractChnl(stack, checkType, 0);
        } catch (NamedProviderGetException e) {
            throw new OperationFailedException(
                    String.format("Cannot extract a single channel from stack %s", stackName),
                    e.summarize());
        }
    }

    public Channel extractChnl(String stackName, boolean checkType, int index)
            throws OperationFailedException {

        try {
            // We make a single histogram
            Stack stack = stackStore.getException(stackName);
            return extractChnl(stack, checkType, index);

        } catch (NamedProviderGetException e) {
            throw new OperationFailedException(
                    String.format("Cannot extract channel %d from stack %s", index, stackName),
                    e.summarize());
        }
    }

    public Channel extractChnl(Stack stack, boolean checkType, int index)
            throws OperationFailedException {
        try {
            Channel chnl = stack.getChannel(index);

            if (checkType) {
                chnlChecker.checkChannelType(chnl.getVoxelDataType());
            }

            return maybeResize(chnl);
        } catch (SetOperationFailedException e) {
            throw new OperationFailedException(
                    String.format("Cannot extract channel %d from stack", index), e);
        }
    }

    private Channel maybeResize(Channel chnl) {
        if (resizeTo.isPresent()) {
            return chnl.resizeXY(resizeTo.get().getWidth(), resizeTo.get().getHeight());
        } else {
            return chnl;
        }
    }

    public NamedImgStackCollection getStackStore() {
        return stackStore;
    }
}
