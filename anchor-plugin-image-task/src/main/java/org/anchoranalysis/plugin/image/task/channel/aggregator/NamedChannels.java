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
package org.anchoranalysis.plugin.image.task.channel.aggregator;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;
import lombok.Getter;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.stack.Stack; // NOSONAR

/**
 * A collection of {@link Channel}s each with an associated unique-name.
 *
 * @author Owen Feehan
 */
public class NamedChannels implements Iterable<Entry<String, Channel>> {

    /** A mapping from name to {@link Channel}, ordered by name. */
    private Map<String, Channel> channels = new TreeMap<>();

    // START REQUIRED ARGUMENTS
    /** Whether the channels originate from a {@link Stack} that is RGB, or not. */
    @Getter private boolean rgb;
    // END REQUIRED ARGUMENTS

    public NamedChannels(boolean rgb) {
        this.rgb = rgb;
    }

    /**
     * Creates by combining multiple existing existing {@link NamedChannels}.
     *
     * @param channelsToCombine the {@link NamedChannels} to combine.
     * @throws OperationFailedException if the RGB-state is inconsistent across {@link Stack}s.
     */
    public NamedChannels(Stream<NamedChannels> channelsToCombine) throws OperationFailedException {

        boolean first = true;

        // The RGB state is true only when true of all channels, false otherwise.
        Iterator<NamedChannels> iterator = channelsToCombine.iterator();
        while (iterator.hasNext()) {

            NamedChannels source = iterator.next();
            if (first) {
                this.rgb = source.rgb;
                first = false;
            } else {
                if (source.rgb != rgb) {
                    throw new OperationFailedException(
                            "The rgb-state must be consistent to combine, but it is not");
                }
            }
            addAll(source);
        }
    }

    @Override
    public Iterator<Entry<String, Channel>> iterator() {
        return channels.entrySet().iterator();
    }

    /**
     * Adds a {@link Channel} with a name.
     *
     * @param name a unique name for the channel.
     * @param channel the channel to add.
     * @throws OperationFailedException if a channel with the same name, has already been previously
     *     added.
     */
    public void add(String name, Channel channel) throws OperationFailedException {
        if (channels.containsKey(name)) {
            throw new OperationFailedException(
                    String.format("A channel with name %s already exists", name));
        }
        channels.put(name, channel);
    }

    /**
     * The names of the {@link Channel}s.
     *
     * @return the channel names.
     */
    public Set<String> names() {
        return channels.keySet();
    }

    /** Add all {@link Channel}s in {@code source}. */
    private void addAll(NamedChannels source) throws OperationFailedException {
        for (Map.Entry<String, Channel> entry : source.channels.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
    }
}
