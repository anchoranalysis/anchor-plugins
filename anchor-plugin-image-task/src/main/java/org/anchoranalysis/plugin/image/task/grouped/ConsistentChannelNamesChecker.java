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

import com.google.common.base.Preconditions;
import java.util.Set;
import lombok.Getter;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.core.channel.Channel; // NOSONAR

/**
 * Checks that each image has an identical set of {@link Channel}-names, and RGB-state.
 *
 * @author Owen Feehan
 */
public class ConsistentChannelNamesChecker {

    /** The names of {@link Channel}s that are consistent across all images. */
    @Getter private Set<String> channelNames;

    /**
     * Whether the {@link Stack} from which the {@link Channel}s originate was RGB or not.
     *
     * <p>This must also be consistent across all images.
     */
    private boolean rgb;

    /**
     * Checks that the channel-names are consistent.
     *
     * @param channelNames the names of the channels to check.
     * @param rgb whether these channels originate from an image that is RGB or not.
     * @throws OperationFailedException if the image do not have identical channel-names or RGB
     *     status.
     */
    public void checkChannelNames(Set<String> channelNames, boolean rgb)
            throws OperationFailedException {
        Preconditions.checkArgument(!channelNames.isEmpty());
        if (this.channelNames == null) {
            this.channelNames = channelNames;
            this.rgb = rgb;
        } else {
            if (!this.channelNames.equals(channelNames)) {
                throw new OperationFailedException(
                        String.format(
                                "All images must have identical channel-names, but they are not consistent: %s versus %s",
                                this.channelNames, channelNames));
            }

            if (this.rgb != rgb) {
                throw new OperationFailedException(
                        "All images must be either RGB, or not-RGB, but a mixture is not allowed");
            }
        }
    }
}
