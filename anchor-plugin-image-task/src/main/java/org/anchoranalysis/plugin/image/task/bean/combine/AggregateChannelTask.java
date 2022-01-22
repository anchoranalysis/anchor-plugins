package org.anchoranalysis.plugin.image.task.bean.combine;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.image.bean.channel.ChannelAggregator;
import org.anchoranalysis.image.bean.spatial.SizeXY;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.stack.named.NamedStacks;
import org.anchoranalysis.io.output.enabled.OutputEnabledMutable;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.plugin.image.task.bean.grouped.GroupedStackBase;
import org.anchoranalysis.plugin.image.task.grouped.ChannelSource;
import org.anchoranalysis.plugin.image.task.grouped.ConsistentChannelChecker;
import org.anchoranalysis.plugin.image.task.grouped.GroupMapByName;
import org.anchoranalysis.plugin.image.task.grouped.GroupedSharedState;
import org.anchoranalysis.plugin.image.task.grouped.NamedChannel;

/**
 * Creates a an aggregated-image for each group, where each voxel-value is aggregated across each
 * channel in each image in the group.
 *
 * <p>Each channel is processed independently.
 *
 * <p>2D images are added in their entirety as one unit.
 *
 * <p>3D images are treated differently, depending on {@code resizeTo}. When not set, they are also
 * added in their entirety. When set, they are projected to 2D via a maximum-intensity-projection
 * and then added (unless {@code slicewise==true}.
 *
 * <p>When a particular input image is errored, it is omitted from the aggregation. The aggregation
 * is still nevertheless produced, if possible from other successful inputs.
 *
 * <p>The following outputs are produced:
 *
 * <table>
 * <caption></caption>
 * <thead>
 * <tr><th>Output Name</th><th>Default?</th><th>Description</th></tr>
 * </thead>
 * <tbody>
 * <tr><td>{@code outputName}</td><td>yes</td><td>An image with the aggregated voxel value for each corresponding voxel.</td></tr>
 * <tr><td rowspan="3"><i>inherited from {@link AggregateChannelTask}</i></td></tr>
 * </tbody>
 * </table>
 *
 * @author Owen Feehan
 */
public class AggregateChannelTask extends GroupedStackBase<Channel, ChannelAggregator> {

    // START BEAN PROPERTIES
    /**
     * If set, each channel is scaled to a specific size before the mean is calculated (useful for
     * combining different sized images)
     */
    @BeanField @OptionalBean @Getter @Setter private SizeXY resizeTo;

    /**
     * When true, a 3D image is added slice-by-slice to the aggregation, treating each slice as a
     * separate image.
     */
    @BeanField @OptionalBean @Getter @Setter private boolean slicewise;

    /** How to aggregate the {@link Channel}s. */
    @BeanField @Getter @Setter private ChannelAggregator aggregator;

    /** How to name the aggregated channel in the output. */
    @BeanField @Getter @Setter private String outputName;
    // END BEAN PROPERTIES

    @Override
    protected void processStacks(
            NamedStacks store,
            Optional<String> groupName,
            GroupedSharedState<Channel, ChannelAggregator> sharedState,
            InputOutputContext context)
            throws JobExecutionException {

        ChannelSource source =
                new ChannelSource(
                        store,
                        sharedState.getChannelChecker(),
                        Optional.ofNullable(resizeTo),
                        getInterpolator().voxelsResizer());

        try {
            for (NamedChannel channel : getSelectChannels().selectChannels(source, true)) {
                addImageToAggregation(sharedState.getGroupMap(), groupName, channel);
            }

        } catch (OperationFailedException e) {
            throw new JobExecutionException(e);
        }
    }

    @Override
    protected Optional<String> subdirectoryForGroupOutputs() {
        return Optional.empty();
    }

    @Override
    protected String outputNameForGroups() {
        return outputName;
    }

    @Override
    public OutputEnabledMutable defaultOutputs() {
        return super.defaultOutputs().addEnabledOutputFirst(outputName);
    }

    @Override
    protected GroupMapByName<Channel, ChannelAggregator> createGroupMap(
            ConsistentChannelChecker channelChecker) {
        return new GroupedChannelAggregator<>(
                outputNameForGroups(), () -> aggregator.duplicateBean());
    }

    /** Adds a particular image to the ongoing aggregation. */
    private void addImageToAggregation(
            GroupMapByName<Channel, ChannelAggregator> groupMap,
            Optional<String> groupName,
            NamedChannel channel)
            throws JobExecutionException {
        if (resizeTo != null) {
            if (slicewise) {
                addImagesSlicewise(groupMap, groupName, channel);
            } else {
                addImageEntirety(
                        groupMap, groupName, channel.getName(), channel.getChannel().projectMax());
            }
        } else {
            addImageEntirety(groupMap, groupName, channel.getName(), channel.getChannel());
        }
    }

    /** Add an image as a whole to the aggregation. */
    private void addImageEntirety(
            GroupMapByName<Channel, ChannelAggregator> groupMap,
            Optional<String> groupName,
            String channelName,
            Channel channel)
            throws JobExecutionException {
        groupMap.add(groupName, channelName, channel);
    }

    /**
     * Add an image slice-by-slice as a whole to the aggregation.
     *
     * <p>When the image is 2D, this is the same as {@link #addImageEntirety(GroupMapByName,
     * Optional, NamedChannel)}.
     */
    private void addImagesSlicewise(
            GroupMapByName<Channel, ChannelAggregator> groupMap,
            Optional<String> groupName,
            NamedChannel channel)
            throws JobExecutionException {
        // Add slice by slice
        int numberSlices = channel.getChannel().extent().z();
        for (int z = 0; z < numberSlices; z++) {
            groupMap.add(groupName, channel.getName(), channel.getChannel().extractSlice(z));
        }
    }
}
