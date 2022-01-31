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
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.image.bean.channel.ChannelAggregator;
import org.anchoranalysis.image.bean.nonbean.ConsistentChannelChecker;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.plugin.image.task.channel.aggregator.NamedChannels;
import org.anchoranalysis.plugin.image.task.grouped.GroupMapByName;

/**
 * Creates a {@link ChannelAggregator} for each group, and writes the aggregated {@link
 * NamedChannels} to the file-system.
 *
 * @param <T> the aggregator that combines {@link NamedChannels}s
 * @author Owen Feehan
 */
class GroupedChannelAggregator<T extends ChannelAggregator> extends GroupMapByName<Channel, T> {

    /** The output-name to use, if not overriden. */
    private final String outputNameDefault;

    /**
     * Create with a particular output-name and method to create aggregators.
     * 
     * @param outputName the first-level output-name used to determine channels will be
     *     written or not.
     * @param createAggregator how to create a new aggregator as needed.
     * @param logger the logger.
     */
    public GroupedChannelAggregator(
            String outputName, Supplier<T> createAggregator, Logger logger) {
        super(
                "channel",
                createAggregator,
                (single, aggregagor) -> aggregagor.addChannel(single, logger));
        this.outputNameDefault = outputName;
    }

    @Override
    protected void outputGroupIntoSubdirectory(
            Collection<Entry<String, T>> namedAggregators,
            ConsistentChannelChecker channelChecker,
            Function<Boolean, InputOutputContext> createContext,
            Optional<String> outputNameSingle)
            throws IOException {

        Optional<Map<String, T>> channelsMap = OutputChannelsAsRGB.canOutputAsRGB(namedAggregators);

        if (channelsMap.isPresent()) {
            OutputChannelsAsRGB.output(
                    name -> channelsMap.get().get(name).aggregatedChannel(),
                    createContext.apply(false),
                    resolveOutputName(outputNameSingle));
        } else {
            OutputChannelsSeparately.output(
                    namedAggregators, () -> resolveOutputName(outputNameSingle), createContext);
        }
    }

    /** Uses {@link outputName} if possible, otherwise fallsback to {@code outputNameDefault}. */
    private String resolveOutputName(Optional<String> outputName) {
        return outputName.orElse(outputNameDefault);
    }
}
