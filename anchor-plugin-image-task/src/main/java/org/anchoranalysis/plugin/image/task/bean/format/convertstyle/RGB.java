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
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.log.MessageLogger;
import org.anchoranalysis.image.core.dimensions.IncorrectImageSizeException;
import org.anchoranalysis.image.core.stack.RGBChannelNames;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.core.stack.named.NamedStacks;
import org.anchoranalysis.plugin.image.task.stack.ChannelGetterForTimepoint;

/**
 * Converts a set of channels to a single RGB-stack.
 *
 * <p>Exactly three channels must be passed to {@link #convert} with names <i>red</i>, <i>green</i>
 * and <i>blue</i> (in any order).
 *
 * <p>If the above condition is not fulfilled, {@link #fallback} is called instead to process the
 * stack.
 *
 * <p>If the RGB-stack is created, it is assigned an empty-string as a name.
 *
 * @author Owen Feehan
 */
public class RGB extends ChannelConvertStyle {

    // START BEAN PROPERTIES
    /**
     * If a channel doesn't match an RGB pattern, this conversion-style can be used instead.
     *
     * <p>If unset, an error is instead thrown in this circumstances
     */
    @BeanField @OptionalBean @Getter @Setter private ChannelConvertStyle fallback;
    // END BEAN PROPERTIES

    @Override
    public NamedStacks convert(
            Set<String> channelNames, ChannelGetterForTimepoint channelGetter, Logger logger)
            throws OperationFailedException {

        if (!channelNamesAreRGB(channelNames)) {
            // Not compatable with RGB
            if (fallback != null) {
                return fallback.convert(channelNames, channelGetter, logger);
            } else {
                throw new OperationFailedException(
                        "Cannot convert as its channels do not look like RGB");
            }
        }

        NamedStacks out = new NamedStacks();

        try {
            Stack stack = createRGBStack(channelGetter, logger.messageLogger());

            // The name is blank as there is a single channel
            out.add("", stack);
        } catch (CreateException e) {
            throw new OperationFailedException("Incorrect image size", e);
        }

        return out;
    }

    private static Stack createRGBStack(
            ChannelGetterForTimepoint channelGetter, MessageLogger logger) throws CreateException {

        Stack stackRearranged = new Stack(true);
        addChannelOrBlank(RGBChannelNames.RED, channelGetter, stackRearranged, logger);
        addChannelOrBlank(RGBChannelNames.GREEN, channelGetter, stackRearranged, logger);
        addChannelOrBlank(RGBChannelNames.BLUE, channelGetter, stackRearranged, logger);
        return stackRearranged;
    }

    private static void addChannelOrBlank(
            String channelName,
            ChannelGetterForTimepoint channelGetter,
            Stack stackRearranged,
            MessageLogger logger)
            throws CreateException {
        try {
            if (channelGetter.hasChannel(channelName)) {
                stackRearranged.addChannel(channelGetter.getChannel(channelName));
            } else {
                logger.logFormatted(String.format("Adding a blank channel for %s", channelName));
                stackRearranged.addBlankChannel();
            }
        } catch (IncorrectImageSizeException
                | OperationFailedException
                | GetOperationFailedException e) {
            throw new CreateException(e);
        }
    }

    private static boolean channelNamesAreRGB(Set<String> channelNames) {

        if (channelNames.size() > 3) {
            return false;
        }

        for (String key : channelNames) {
            // If a key doesn't match one of the expected red-green-blue names
            if (!(key.equals(RGBChannelNames.RED)
                    || key.equals(RGBChannelNames.GREEN)
                    || key.equals(RGBChannelNames.BLUE))) {
                return false;
            }
        }

        return true;
    }
}
