/* (C)2020 */
package org.anchoranalysis.plugin.image.task.bean.grouped.raster;

import java.util.Optional;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.image.bean.size.SizeXY;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.stack.NamedImgStackCollection;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.plugin.image.task.bean.grouped.GroupedStackTask;
import org.anchoranalysis.plugin.image.task.grouped.ChannelSource;
import org.anchoranalysis.plugin.image.task.grouped.ConsistentChannelChecker;
import org.anchoranalysis.plugin.image.task.grouped.GroupMapByName;
import org.anchoranalysis.plugin.image.task.grouped.GroupedSharedState;
import org.anchoranalysis.plugin.image.task.grouped.NamedChnl;

/**
 * Creates a an aggregated-image for each group, where each voxel-value is the mean voxel-value
 * across the group.
 *
 * @author Owen Feehan
 */
public class GroupedMeanChnlTask extends GroupedStackTask<Channel, AggregateChnl> {

    // START BEAN PROPERTIES
    /**
     * If set, each channel is scaled to a specific size before the mean is calculated (useful for
     * combining different sized images)
     */
    @BeanField @OptionalBean private SizeXY resizeTo;
    // END BEAN PROPERTIES

    @Override
    protected GroupMapByName<Channel, AggregateChnl> createGroupMap(
            ConsistentChannelChecker chnlChecker) {
        return new GroupedMeanChnlMap();
    }

    @Override
    protected void processKeys(
            NamedImgStackCollection store,
            Optional<String> groupName,
            GroupedSharedState<Channel, AggregateChnl> sharedState,
            BoundIOContext context)
            throws JobExecutionException {

        ChannelSource source =
                new ChannelSource(
                        store, sharedState.getChnlChecker(), Optional.ofNullable(resizeTo));

        try {
            for (NamedChnl chnl : getSelectChnls().selectChnls(source, true)) {
                sharedState.getGroupMap().add(groupName, chnl.getName(), chnl.getChnl());
            }

        } catch (OperationFailedException e) {
            throw new JobExecutionException(e);
        }
    }

    @Override
    protected Optional<String> subdirectoryForGroupOutputs() {
        return Optional.empty();
    }

    public SizeXY getResizeTo() {
        return resizeTo;
    }

    public void setResizeTo(SizeXY resizeTo) {
        this.resizeTo = resizeTo;
    }
}
