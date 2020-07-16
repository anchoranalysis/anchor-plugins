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
