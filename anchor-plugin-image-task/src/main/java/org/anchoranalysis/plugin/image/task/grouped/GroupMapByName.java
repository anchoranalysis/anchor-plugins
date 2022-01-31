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

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.checked.CheckedBiConsumer;
import org.anchoranalysis.core.identifier.name.MapCreate;
import org.anchoranalysis.feature.io.name.MultiName;
import org.anchoranalysis.feature.io.name.MultiNameFactory;
import org.anchoranalysis.image.bean.nonbean.ConsistentChannelChecker;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.io.output.outputter.InputOutputContext;

/**
 * Adds items to aggregate structures identified uniquely by a name, and allows these items to be
 * later outputted.
 *
 * <p>If any of the {@link MultiName} have a higher level aggregation-key, this is used to partition
 * the output at the end into sub-directories.
 *
 * @author Owen Feehan
 * @param <S> single item type
 * @param <T> aggregator type
 */
public abstract class GroupMapByName<S, T> {

    /**
     * A map of maps, indexing first by the <b>first part</b> of the {@link MultiName} and secondly
     * by the <b>second part</b>.
     */
    private final MapCreate<Optional<String>, MapCreate<String, T>> map;

    private final String nounT;

    /** Adds a single-item into an aggregator. */
    private final CheckedBiConsumer<S, T, OperationFailedException> addSingleToAggregator;

    /**
     * Creates a group-map.
     *
     * @param nounT a word to describe a single instance of T in user error messages.
     * @param addSingleToAggregator adds a single-item into an aggregator.
     * @param createAggregator called to create a new aggregator, whenever needed e.g. for a
     *     particular group.
     */
    protected GroupMapByName(
            String nounT,
            Supplier<T> createAggregator,
            CheckedBiConsumer<S, T, OperationFailedException> addSingleToAggregator) {
        this.map = new MapCreate<>(() -> new MapCreate<>(createAggregator));
        this.nounT = nounT;
        this.addSingleToAggregator = addSingleToAggregator;
    }

    /**
     * Adds an item with a non-group identifier, and also optionally a group identifier.
     *
     * @throws OperationFailedException if the operation cannot successfully complete.
     */
    public synchronized void add(
            Optional<String> groupIdentifier, String nonGroupIdentifier, S singleItemToAdd)
            throws OperationFailedException {
        try {
            // Get the correct aggregate structure
            T aggregator = map.computeIfAbsent(groupIdentifier).computeIfAbsent(nonGroupIdentifier);
            addSingleToAggregator.accept(singleItemToAdd, aggregator);

        } catch (OperationFailedException e) {

            MultiName identifier = MultiNameFactory.create(groupIdentifier, nonGroupIdentifier);
            throw new OperationFailedException(
                    String.format(
                            "An error occurred combining the %s created for: %s",
                            nounT, identifier),
                    e);
        }
    }

    /**
     * Outputs the "grouped" data to the filesystem.
     *
     * @param channelChecker what checks that {@link Channel}s are consistent.
     * @param context where to perform the outputting.
     * @throws IOException if outputting doesn't occur successfully.
     */
    public void outputGroupedData(
            ConsistentChannelChecker channelChecker, InputOutputContext context)
            throws IOException {

        // If there is a second part-only in the MultiName, it is assumed that there is no group
        // (for all items) and it is
        // written without a subdirectory
        // If there are two parts in the MultiName, it is assumed that the first-part is a
        // group-name (a separate
        // subdirectory) and the second-part is written without a subdirectory

        // Process each output subdirectory collectively
        for (Map.Entry<Optional<String>, MapCreate<String, T>> entryGroup : map.entrySet()) {
            outputGroupIntoSubdirectory(
                    entryGroup.getValue().entrySet(),
                    channelChecker,
                    multipleOutputs ->
                            maybeCreateSubdirectory(multipleOutputs, context, entryGroup.getKey()),
                    entryGroup.getKey());
        }
    }

    /**
     * Output a particular group into a subdirectory.
     *
     * @param namedAggregators all the aggregators for this group.
     * @param channelChecker what was used to ensure all {@link Channel}s had identical attributes.
     * @param createContext the subdirectory into which outputting occurs, given a boolean which is
     *     true (when multiple outputs occur), or false (when a single output occurs).
     * @param outputNameSingle the output-name to use if there is only a single output, (in which
     *     case {@code createContext} should always be called with false).
     * @throws IOException if unable to output successfully.
     */
    protected abstract void outputGroupIntoSubdirectory(
            Collection<Map.Entry<String, T>> namedAggregators,
            ConsistentChannelChecker channelChecker,
            Function<Boolean, InputOutputContext> createContext,
            Optional<String> outputNameSingle)
            throws IOException;

    /**
     * Creates a subdirectory if grouping is occuring <b>and</b> multipleOutputs occur.
     *
     * @param multipleOutputs whether each group will produce multiple outputs or a single output.
     * @param context the context in which to maybe create a subdirectory, or else use as-is.
     * @param groupKey the key associated with the particular group.
     */
    private static InputOutputContext maybeCreateSubdirectory(
            boolean multipleOutputs, InputOutputContext context, Optional<String> groupKey) {
        if (groupKey.isPresent() && multipleOutputs) {
            return context.subdirectory(groupKey.get(), false);
        } else {
            return context;
        }
    }
}
