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

package org.anchoranalysis.plugin.image.task.bean.grouped.raster;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.image.bean.spatial.SizeXY;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.stack.NamedStacks;
import org.anchoranalysis.io.output.enabled.OutputEnabledMutable;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.plugin.image.task.bean.grouped.GroupedStackBase;
import org.anchoranalysis.plugin.image.task.grouped.ChannelSource;
import org.anchoranalysis.plugin.image.task.grouped.ConsistentChannelChecker;
import org.anchoranalysis.plugin.image.task.grouped.GroupMapByName;
import org.anchoranalysis.plugin.image.task.grouped.GroupedSharedState;
import org.anchoranalysis.plugin.image.task.grouped.NamedChannel;

/**
 * Creates a an aggregated-image for each group, where each voxel-value is the mean voxel-value
 * across the group.
 *
 * @author Owen Feehan
 */
public class GroupedMeanChannelTask extends GroupedStackBase<Channel, RunningSumChannel> {

    // START BEAN PROPERTIES
    /**
     * If set, each channel is scaled to a specific size before the mean is calculated (useful for
     * combining different sized images)
     */
    @BeanField @OptionalBean @Getter @Setter private SizeXY resizeTo;
    // END BEAN PROPERTIES

    @Override
    public OutputEnabledMutable defaultOutputs() {
        assert (false);
        return super.defaultOutputs();
    }

    @Override
    protected GroupMapByName<Channel, RunningSumChannel> createGroupMap(
            ConsistentChannelChecker channelChecker) {
        return new GroupedMeanChannelMap();
    }

    @Override
    protected void processStacks(
            NamedStacks store,
            Optional<String> groupName,
            GroupedSharedState<Channel, RunningSumChannel> sharedState,
            InputOutputContext context)
            throws JobExecutionException {

        ChannelSource source =
                new ChannelSource(
                        store, sharedState.getChannelChecker(), Optional.ofNullable(resizeTo));

        try {
            for (NamedChannel channel : getSelectChannels().selectChannels(source, true)) {
                sharedState.getGroupMap().add(groupName, channel.getName(), channel.getChannel());
            }

        } catch (OperationFailedException e) {
            throw new JobExecutionException(e);
        }
    }

    @Override
    protected Optional<String> subdirectoryForGroupOutputs() {
        return Optional.empty();
    }
}
