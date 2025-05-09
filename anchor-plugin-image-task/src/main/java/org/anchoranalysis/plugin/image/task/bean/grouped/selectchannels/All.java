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

package org.anchoranalysis.plugin.image.task.bean.grouped.selectchannels;

import java.util.Set;
import java.util.stream.Stream;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.CheckedStream;
import org.anchoranalysis.core.identifier.provider.NamedProviderGetException;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.plugin.image.task.channel.aggregator.NamedChannels;
import org.anchoranalysis.plugin.image.task.grouped.ChannelSource;

/**
 * Selects all possible channels from all possible stacks
 *
 * <p>If a stack has a single-channel, it it uses this name as an output If a stack has multiple
 * channels, this name is used but suffixed with a number of each channel (00, 01 etc.)
 *
 * @author Owen Feehan
 */
public class All extends FromStacks {

    @Override
    public NamedChannels selectChannels(ChannelSource source, boolean checkType)
            throws OperationFailedException {

        Set<String> keys = source.getStackStore().keys();

        Stream<NamedChannels> stream =
                CheckedStream.map(
                        keys.stream(),
                        OperationFailedException.class,
                        key -> extractAllChannels(source, key, checkType));
        return new NamedChannels(stream);
    }

    private NamedChannels extractAllChannels(
            ChannelSource source, String stackName, boolean checkType)
            throws OperationFailedException {

        try {
            // We make a single histogram
            Stack stack = source.getStackStore().getException(stackName);

            NamedChannels out = new NamedChannels(stack.isRGB());
            for (int i = 0; i < stack.getNumberChannels(); i++) {

                String outputName = stackName + createSuffix(i, stack.getNumberChannels() > 1);
                out.add(outputName, source.extractChannel(stack, checkType, i));
            }
            return out;

        } catch (NamedProviderGetException e) {
            throw new OperationFailedException(e.summarize());
        }
    }

    private static String createSuffix(int index, boolean hasMultipleChannels) {
        if (hasMultipleChannels) {
            return String.format("%02d", index);
        } else {
            return "";
        }
    }
}
