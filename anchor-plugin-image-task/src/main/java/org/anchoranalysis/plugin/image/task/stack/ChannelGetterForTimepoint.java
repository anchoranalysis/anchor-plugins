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

package org.anchoranalysis.plugin.image.task.stack;

import lombok.AllArgsConstructor;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.io.channel.input.ChannelGetter;

/** A wrapper around {@link ChannelGetter} that retrieves channels for a specific timepoint. */
@AllArgsConstructor
public class ChannelGetterForTimepoint {

    /** The underlying {@link ChannelGetter} to retrieve channels. */
    private ChannelGetter getter;

    /** The timepoint for which to retrieve channels. */
    private int t;

    /**
     * Checks if a channel with the given name exists.
     *
     * @param channelName the name of the channel to check
     * @return true if the channel exists, false otherwise
     */
    public boolean hasChannel(String channelName) {
        return getter.hasChannel(channelName);
    }

    /**
     * Retrieves a channel with the given name for the specified timepoint.
     *
     * @param channelName the name of the channel to retrieve
     * @param logger the logger to use for any logging operations
     * @return the {@link Channel} for the specified name and timepoint
     * @throws GetOperationFailedException if the channel retrieval operation fails
     */
    public Channel getChannel(String channelName, Logger logger)
            throws GetOperationFailedException {
        return getter.getChannel(channelName, t, logger);
    }
}
