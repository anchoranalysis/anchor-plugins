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

import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.plugin.image.task.channel.aggregator.NamedChannels;
import org.anchoranalysis.plugin.image.task.grouped.ChannelSource;

/** Selects a subset of channels from a set of stacks. */
public abstract class FromStacks extends AnchorBean<FromStacks> {

    /**
     * Takes a stack-collection and extracts a set of references to particular channels (each with a
     * name).
     *
     * @param source the {@link ChannelSource} containing the stacks to select from
     * @param checkType whether to check the type of the channels
     * @return a {@link NamedChannels} object containing the selected channels
     * @throws OperationFailedException if the channel selection operation fails
     */
    public abstract NamedChannels selectChannels(ChannelSource source, boolean checkType)
            throws OperationFailedException;
}
