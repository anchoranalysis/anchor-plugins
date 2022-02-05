package org.anchoranalysis.plugin.image.task.bean.combine;

import java.util.Optional;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.checked.CheckedBiConsumer;
import org.anchoranalysis.core.functional.checked.CheckedFunction;
import org.anchoranalysis.core.time.OperationContext;
import org.anchoranalysis.image.bean.channel.ChannelAggregator;
import org.anchoranalysis.image.bean.nonbean.ConsistentChannelChecker;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.io.output.enabled.OutputEnabledMutable;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.plugin.image.task.bean.grouped.GroupedStackBase;
import org.anchoranalysis.plugin.image.task.grouped.ChannelSource;
import org.anchoranalysis.plugin.image.task.grouped.GroupMapByName;
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
    public OutputEnabledMutable defaultOutputs() {
        return super.defaultOutputs().addEnabledOutputFirst(outputName);
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
    protected GroupMapByName<Channel, ChannelAggregator> createGroupMap(
            ConsistentChannelChecker channelChecker,
            Optional<Stream<String>> groupIdentifiers,
            Optional<InputOutputContext> outputContext,
            OperationContext operationContext) {
        return new GroupedChannelAggregator<>(
                outputNameForGroups(),
                groupIdentifiers,
                outputContext,
                () -> aggregator.duplicateBean(),
                operationContext.getLogger());
    }

    @Override
    protected void processIndividual(
            String name,
            Channel individual,
            boolean partOfGroup,
            CheckedBiConsumer<String, Channel, OperationFailedException> consumeIndividual,
            InputOutputContext context)
            throws OperationFailedException {

        if (getResizeTo() != null) {
            if (slicewise) {
                addImagesSlicewise(consumeIndividual, name, individual);
            } else {
                addImageEntirety(consumeIndividual, name, individual.projectMax());
            }
        } else {
            addImageEntirety(consumeIndividual, name, individual);
        }
    }

    @Override
    protected CheckedFunction<Channel, Channel, CreateException> createChannelDeriver(
            ChannelSource source) throws OperationFailedException {
        return channel -> channel;
    }

    /** Add an image as a whole to the aggregation. */
    private void addImageEntirety(
            CheckedBiConsumer<String, Channel, OperationFailedException> addChannelToMap,
            String channelName,
            Channel channel)
            throws OperationFailedException {
        addChannelToMap.accept(channelName, channel);
    }

    /**
     * Add an image slice-by-slice as a whole to the aggregation.
     *
     * <p>When the image is 2D, this is the same as {@link #addImageEntirety(GroupMapByName,
     * Optional, NamedChannel)}.
     */
    private void addImagesSlicewise(
            CheckedBiConsumer<String, Channel, OperationFailedException> addChannelToMap,
            String name,
            Channel channel)
            throws OperationFailedException {
        // Add slice by slice
        int numberSlices = channel.extent().z();
        for (int z = 0; z < numberSlices; z++) {
            addChannelToMap.accept(name, channel.extractSlice(z));
        }
    }
}
