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

import java.util.function.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.anchoranalysis.image.bean.nonbean.ConsistentChannelChecker;
import org.anchoranalysis.image.core.channel.Channel; // NOSONAR

/**
 * Commonality between shared state for grouped export tasks.
 *
 * @author Owen Feehan
 * @param <S> individual-type
 * @param <T> aggregate-type
 */
@RequiredArgsConstructor
public class GroupedSharedState<S, T> {

    /** Checks that {@link Channel}s have a consistent voxel-data type. */
    @Getter private ConsistentChannelChecker channelChecker = new ConsistentChannelChecker();

    /**
     * Checks that each image provides a consistent set of channels, and that they have the same
     * RGB-state.
     */
    @Getter
    private ConsistentChannelNamesChecker channelNamesChecker = new ConsistentChannelNamesChecker();

    private GroupMapByName<S, T> groupMap;

    // START REQUIRED ARGUMENTS
    /** How to create the group-map when needed. */
    private final Function<ConsistentChannelChecker, GroupMapByName<S, T>> createGroupMap;
    // END REQUIRED ARGUMENTS

    public synchronized GroupMapByName<S, T> getGroupMap() {

        if (groupMap == null) {
            this.groupMap = createGroupMap.apply(channelChecker);
        }

        return groupMap;
    }
}
