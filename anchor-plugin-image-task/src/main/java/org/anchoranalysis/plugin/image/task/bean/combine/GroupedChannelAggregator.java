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

package org.anchoranalysis.plugin.image.task.bean.combine;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.exception.friendly.AnchorImpossibleSituationException;
import org.anchoranalysis.image.bean.channel.ChannelAggregator;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.dimensions.IncorrectImageSizeException;
import org.anchoranalysis.image.core.stack.RGBChannelNames;
import org.anchoranalysis.image.core.stack.RGBStack;
import org.anchoranalysis.image.io.channel.output.ChannelGenerator;
import org.anchoranalysis.image.io.stack.output.generator.StackGenerator;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.plugin.image.task.grouped.ConsistentChannelChecker;
import org.anchoranalysis.plugin.image.task.grouped.GroupMapByName;
import org.apache.commons.math3.util.Pair;

/**
 * Creates a {@link ChannelAggregator} for each group, and writes the aggregated {@link Channel} to
 * the file-system.
 *
 * @param <T> the aggregator that combines {@link Channel}s
 * @author Owen Feehan
 */
class GroupedChannelAggregator<T extends ChannelAggregator> extends GroupMapByName<Channel, T> {

    private final String outputName;
    /**
     * @param outputName the first-level output-name used to determine whether mean channels will be
     *     written or not
     */
    public GroupedChannelAggregator(String outputName, Supplier<T> createAggregator) {
        super("channel", createAggregator);
        this.outputName = outputName;
    }

    @Override
    protected void addTo(Channel channelToAdd, T aggregator) throws OperationFailedException {
        aggregator.addChannel(channelToAdd);
    }

    @Override
    protected void outputGroupIntoSubdirectory(
            Collection<Pair<String, T>> namedAggregators,
            ConsistentChannelChecker channelChecker,
            InputOutputContext subdirectory)
            throws IOException {

        if (namedAggregators.size() == 3) {
            // Consider writing the channels together as an RGB stack rather than separately
            // If there is an alpha channel with the RGB, it is ignored.

            // Build a map between each channel-name and the aggregator, and check if the keys of
            // the map correspond
            // exactly to what we expect in RGB
            Map<String, T> channelsMap =
                    namedAggregators.stream()
                            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
            if (RGBChannelNames.isValidNameSet(channelsMap.keySet(), false)
                    || RGBChannelNames.isValidNameSet(channelsMap.keySet(), true)) {
                outputChannelsAsRGB(channelsMap, subdirectory);
                return;
            }
        }
        outputChannelsSeparately(namedAggregators, subdirectory);
    }

    /** Write each {@link Channel} separately into the group subdirectory. */
    private void outputChannelsSeparately(
            Collection<Pair<String, T>> namedAggregators, InputOutputContext subdirectory) {
        // We can write these group outputs in parallel, as we no longer in the parallel part of
        // Anchor's task execution
        namedAggregators.parallelStream()
                .forEach(
                        pair ->
                                subdirectory
                                        .getOutputter()
                                        .writerSecondLevel(outputName)
                                        .write(
                                                pair.getFirst(),
                                                ChannelGenerator::new,
                                                () -> aggregatedChannel(pair.getSecond())));
    }

    private void outputChannelsAsRGB(Map<String, T> channels, InputOutputContext subdirectory)
            throws IOException {
        try {
            RGBStack stack =
                    new RGBStack(
                            extractChannel(channels, RGBChannelNames.RED),
                            extractChannel(channels, RGBChannelNames.GREEN),
                            extractChannel(channels, RGBChannelNames.BLUE));

            subdirectory
                    .getOutputter()
                    .writerPermissive()
                    .write(outputName, () -> new StackGenerator(true), stack::asStack);

        } catch (OperationFailedException e) {
            throw new IOException(
                    "Unable to extract a particular color channel to output an aggregate as a RGB",
                    e);
        } catch (IncorrectImageSizeException e) {
            throw new AnchorImpossibleSituationException();
        }
    }

    private Channel extractChannel(Map<String, T> channels, String channelName)
            throws OperationFailedException {
        return channels.get(channelName).aggregatedChannel();
    }

    private Channel aggregatedChannel(T aggregator) throws OutputWriteFailedException {
        try {
            return aggregator.aggregatedChannel();
        } catch (OperationFailedException e) {
            throw new OutputWriteFailedException("Failed to create aggregated channel", e);
        }
    }
}
