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

import java.util.Set;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.image.core.stack.named.NamedStacks;
import org.anchoranalysis.plugin.image.task.stack.ChannelGetterForTimepoint;

/**
 * Converts a channel(s) at a particular timepoint into a stack(s).
 *
 * <p>Whether each channel becomes its own single-channeled stack, or is combined to form
 * multi-channeled stacks can vary by sub-class implementation.
 *
 * <p>A unique-name (the channel-name) is assigned for each stack created, including possibly an
 * empty string.
 *
 * @author Owen Feehan
 */
public abstract class ChannelConvertStyle extends AnchorBean<ChannelConvertStyle> {

    /**
     * Converts a particular set of channels.
     *
     * @param channelNames a set of names of the channels to convert.
     * @param channelGetter gets a particular channel at a particular time-point.
     * @param logger the logger.
     * @return the results of the conversion.
     * @throws OperationFailedException if the conversion fails to successfully complete.
     */
    public abstract NamedStacks convert(
            Set<String> channelNames, ChannelGetterForTimepoint channelGetter, Logger logger)
            throws OperationFailedException;
}
