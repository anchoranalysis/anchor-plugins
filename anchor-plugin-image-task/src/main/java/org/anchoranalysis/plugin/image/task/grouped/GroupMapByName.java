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

package org.anchoranalysis.plugin.image.task.grouped;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import java.io.IOException;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Supplier;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.identifier.name.MapCreate;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.feature.io.name.MultiName;
import org.anchoranalysis.feature.io.name.MultiNameFactory;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.io.output.outputter.InputOutputContextSubdirectoryCache;
import org.apache.commons.math3.util.Pair;

/**
 * Adds items to aggregate structures identified uniquely by a name, and allows these items to be
 * later outputted.
 *
 * <p>If any of the {@link MultiName} have a higher level aggregation-key, this is used to partition
 * the output at the end into sub-directories.
 *
 * @author Owen Feehan
 * @param <S> individual-item type
 * @param <T> aggregate-item type (combines many individual types)
 */
public abstract class GroupMapByName<S, T> {

    private final MapCreate<MultiName, T> map;

    private final String nounT;

    /**
     * Creates a group-map.
     *
     * @param nounT a word to describe a single instance of T in user error messages.
     * @param createAggregator called to create a new aggregator, whenever needed e.g. for a
     *     particular group.
     */
    protected GroupMapByName(String nounT, Supplier<T> createAggregator) {
        this.map = new MapCreate<>(createAggregator);
        this.nounT = nounT;
    }

    /**
     * Adds an item with an identifier
     *
     * @throws JobExecutionException
     */
    public synchronized void add(
            Optional<String> groupIdentifier, String nonGroupIdentifier, S itemToAdd)
            throws JobExecutionException {

        MultiName identifier = MultiNameFactory.create(groupIdentifier, nonGroupIdentifier);

        try {
            addTo(itemToAdd, map.computeIfAbsent(identifier));

        } catch (OperationFailedException e) {
            throw new JobExecutionException(
                    String.format(
                            "An error occurred combining the %s created for: %s",
                            nounT, identifier),
                    e);
        }
    }

    /**
     * Outputs the "grouped" data to the filesystem
     *
     * @param channelChecker channel checker
     * @param context
     * @throws IOException if something goes wrong, or if includeGroupName is false, but more than
     *     one group-names exist
     */
    public void outputGroupedData(
            ConsistentChannelChecker channelChecker, InputOutputContext context)
            throws IOException {

        // We wish to create a new output-manager only once for each primary key, so we store them
        // in a hashmap
        InputOutputContextSubdirectoryCache subdirectoryCache =
                new InputOutputContextSubdirectoryCache(context, false);

        // If there is one part-only, it is assumed that there is no group (for all items) and it is
        // written without a subdirectory
        // If there are two parts, it is assumed that the first-part is a group-name (a separate
        // subdirectory) and the second-part is written without a subdirectory

        // Rather than write each entry, individually, we want to write them one directory at a time
        // to give the implementation a chance, to process multiple entries for the same directory
        // together.

        // We create a list of all entries, sorted by their subdirectory context
        Multimap<InputOutputContext, Pair<String, T>> indexedBySubdirectory =
                MultimapBuilder.hashKeys().arrayListValues().build();
        for (Entry<MultiName, T> entry : map.entrySet()) {

            MultiName name = entry.getKey();
            indexedBySubdirectory.put(
                    subdirectoryCache.get(name.firstPart()),
                    new Pair<>(name.secondPart(), entry.getValue()));
        }

        // Process each output subdirectory collectively
        for (InputOutputContext subdirectory : indexedBySubdirectory.keySet()) {
            outputGroupIntoSubdirectory(
                    indexedBySubdirectory.get(subdirectory), channelChecker, subdirectory);
        }
    }

    protected abstract void addTo(S channelToAdd, T aggregator) throws OperationFailedException;

    /**
     * Output a particular group into a subdirectory.
     *
     * @param namedAggregators all the aggregators for this group.
     * @param channelChecker what was used to ensure all {@link Channel}s had identical attributes.
     * @param subdirectory the subdirectory into which outputting occurs.
     * @throws IOException if unable to output successfully.
     */
    protected abstract void outputGroupIntoSubdirectory(
            Collection<Pair<String, T>> namedAggregators,
            ConsistentChannelChecker channelChecker,
            InputOutputContext subdirectory)
            throws IOException;
}
