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

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.plugin.image.task.channel.aggregator.NamedChannels;
import org.anchoranalysis.plugin.image.task.grouped.ChannelSource;

/**
 * Selects one specific channel from a set of stacks
 *
 * @author Owen Feehan
 */
public class One extends FromStacks {

    // START BEAN FIELDS
    /**
     * If defined, processing only occurs the stack with this specific name and index.
     * 
     * <p>Otherwise processing occurs on all input stacks.
     */
    @BeanField @Getter @Setter private String stackName;

    /**
     * The index of the channel to retrieve from {@code stackName}.
     */
    @BeanField @Getter @Setter private int channelIndex = 0;
    // END BEAN FIELDS

    @Override
    public NamedChannels selectChannels(ChannelSource source, boolean checkType)
            throws OperationFailedException {
        Channel channel = source.extractChannel(stackName, checkType, channelIndex);

        NamedChannels out = new NamedChannels(false);
        out.add(stackName, channel);
        return out;
    }
}
