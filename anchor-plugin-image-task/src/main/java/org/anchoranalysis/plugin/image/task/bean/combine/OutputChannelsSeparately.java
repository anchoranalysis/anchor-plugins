/*-
 * #%L
 * anchor-plugin-image-task
 * %%
 * Copyright (C) 2010 - 2022 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.bean.channel.ChannelAggregator;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.io.channel.output.ChannelGenerator;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.plugin.image.task.channel.aggregator.NamedChannels;

/**
 * Write {@code channels} to the file-system as separate channels.
 *
 * @name Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class OutputChannelsSeparately {

    /**
     * Write each {@link Channel} separately into a subdirectory.
     *
     * @param <T> the aggregator that combines {@link NamedChannels}s
     * @param namedAggregators the aggregators, each with an associated name.
     * @param outputName the name to use for the channel, if only a single output is written.
     * @param context the subdirectory to write to.
     */
    public static <T extends ChannelAggregator> void output(
            Collection<Entry<String, T>> namedAggregators,
            Supplier<String> outputNameSingle,
            Function<Boolean, InputOutputContext> createContext) {
        if (namedAggregators.size() > 1) {
            // Write using the name of each aggregator as there are multiple aggregators.
            InputOutputContext context = createContext.apply(true);
            outputIntoContext(namedAggregators, Optional.empty(), context);
        } else {
            // Write using outputNameSingle as there is only a single aggregator.
            InputOutputContext context = createContext.apply(false);
            outputIntoContext(namedAggregators, Optional.of(outputNameSingle.get()), context);
        }
    }

    /**
     * Write each {@link Channel} separately into a subdirectory (the context).
     *
     * @param <T> the aggregator that combines {@link NamedChannels}s
     * @param namedAggregators the aggregators, each with an associated name.
     * @param outputName the name to use for the channel, if defined. If not defined, the name of
     *     the aggregator is used.
     * @param context the subdirectory to write to.
     */
    private static <T extends ChannelAggregator> void outputIntoContext(
            Collection<Entry<String, T>> namedAggregators,
            Optional<String> outputName,
            InputOutputContext context) {
        // We can write these group outputs in parallel, as we no longer in the parallel part of
        // Anchor's task execution
        namedAggregators.parallelStream()
                .forEach(
                        entry ->
                                writeChannel(
                                        outputName.orElse(entry.getKey()),
                                        entry.getValue(),
                                        context));
    }

    /** Writes a {@link Channel} into a subdirectory. */
    private static <T extends ChannelAggregator> void writeChannel(
            String outputName, T aggregator, InputOutputContext context) {
        context.getOutputter()
                .writerSecondLevel(outputName)
                .write(outputName, ChannelGenerator::new, () -> extractChannel(aggregator));
    }

    /**
     * Gets the aggregated-channel from an {@code aggregator} throwing an {@code
     * OutputWriteFailedException} if failure occurs.
     */
    private static <T extends ChannelAggregator> Channel extractChannel(T aggregator)
            throws OutputWriteFailedException {
        try {
            return aggregator.aggregatedChannel();
        } catch (OperationFailedException e) {
            throw new OutputWriteFailedException("Failed to create aggregated channel", e);
        }
    }
}
