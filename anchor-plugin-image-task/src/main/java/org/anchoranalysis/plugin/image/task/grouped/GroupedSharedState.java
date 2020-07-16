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
/* (C)2020 */
package org.anchoranalysis.plugin.image.task.grouped;

import java.util.function.Function;

/**
 * Commonality between shared state for gouped export tasks
 *
 * @author Owen Feehan
 * @param <S> individual-type
 * @param <T> aggregate-type
 */
public class GroupedSharedState<S, T> {

    private ConsistentChannelChecker chnlChecker = new ConsistentChannelChecker();

    private GroupMapByName<S, T> groupMap;

    private Function<ConsistentChannelChecker, GroupMapByName<S, T>> createGroupMap;

    public GroupedSharedState(
            Function<ConsistentChannelChecker, GroupMapByName<S, T>> createGroupMap) {
        this.createGroupMap = createGroupMap;
    }

    public ConsistentChannelChecker getChnlChecker() {
        return chnlChecker;
    }

    public GroupMapByName<S, T> getGroupMap() {

        if (groupMap == null) {
            this.groupMap = createGroupMap.apply(chnlChecker);
        }

        return groupMap;
    }
}
